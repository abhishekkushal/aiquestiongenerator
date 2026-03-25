package com.exam.aiquestions.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exam.aiquestions.model.Question;
import com.exam.aiquestions.model.QuestionType;
import com.exam.aiquestions.model.ReviewStatus;
import com.exam.aiquestions.repository.QuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuestionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private QuestionRepository questionRepository;

    public Map<String, Object> generateQuestions(String topic, QuestionType questionType) {

        List<String> chunks = vectorSearchService.search(topic);
        String context = String.join("\n", chunks);

        List<LLMService.QuestionCandidate> candidates = llmService.generateQuestions(
                context, topic, questionType);

        List<Question> savedQuestions = new ArrayList<>();
        Set<String> batchHashes = new HashSet<>();
        int skippedDuplicates = 0;

        for (LLMService.QuestionCandidate candidate : candidates) {
            if (!isValid(candidate, questionType)) {
                continue;
            }

            String questionHash = buildQuestionHash(questionType, candidate.getQuestionText());
            if (!batchHashes.add(questionHash) || questionRepository.existsByQuestionHash(questionHash)) {
                skippedDuplicates++;
                continue;
            }

            Question question = new Question();
            question.setTopic(topic.trim());
            question.setQuestionType(questionType);
            question.setQuestionText(candidate.getQuestionText().trim());
            question.setComplexity(candidate.getComplexity());
            question.setReviewStatus(ReviewStatus.DRAFT);
            question.setExplanation(normalizeOptional(candidate.getExplanation()));
            question.setQuestionHash(questionHash);
            question.setAnswerData(toAnswerData(candidate, questionType));

            savedQuestions.add(questionRepository.save(question));
        }

        return Map.of(
                "topic", topic,
                "questionType", questionType,
                "requestedCount", LLMService.QUESTION_COUNT,
                "savedCount", savedQuestions.size(),
                "skippedDuplicates", skippedDuplicates,
                "questions", savedQuestions.stream().map(this::toResponse).toList());
    }

    public List<Map<String, Object>> listQuestions(QuestionType questionType, ReviewStatus reviewStatus, String topic) {
        return questionRepository.findForBank(questionType, reviewStatus, normalizeOptional(topic))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<String, Object> reviewQuestion(Long id, ReviewStatus reviewStatus, String reviewedBy) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
        question.setReviewStatus(reviewStatus);
        question.setReviewedAt(LocalDateTime.now());
        question.setReviewedBy(reviewedBy == null || reviewedBy.isBlank() ? "Reviewer" : reviewedBy.trim());
        return toResponse(questionRepository.save(question));
    }

    private boolean isValid(LLMService.QuestionCandidate candidate, QuestionType questionType) {
        if (candidate == null || isBlank(candidate.getQuestionText()) || candidate.getComplexity() == null) {
            return false;
        }

        return switch (questionType) {
            case MCQ -> candidate.getOptions() != null
                    && candidate.getOptions().size() == 4
                    && isOption(candidate.getCorrectOption());
            case TRUE_FALSE -> candidate.getBooleanAnswer() != null;
            case FILL_BLANK -> candidate.getAcceptedAnswers() != null
                    && !candidate.getAcceptedAnswers().isEmpty()
                    && candidate.getQuestionText().contains("_____");
            case NUMERIC_RANGE -> candidate.getMinValue() != null && candidate.getMaxValue() != null;
            case SHORT_ANSWER -> candidate.getRubricKeywords() != null
                    && !candidate.getRubricKeywords().isEmpty()
                    && !isBlank(candidate.getModelAnswer());
        };
    }

    private boolean isOption(String value) {
        if (isBlank(value)) {
            return false;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("A") || normalized.equals("B") || normalized.equals("C") || normalized.equals("D");
    }

    private String toAnswerData(LLMService.QuestionCandidate candidate, QuestionType questionType) {
        Map<String, Object> answerData = new LinkedHashMap<>();

        switch (questionType) {
            case MCQ -> {
                answerData.put("options", candidate.getOptions());
                answerData.put("correctOption", candidate.getCorrectOption().trim().toUpperCase(Locale.ROOT));
            }
            case TRUE_FALSE -> {
                answerData.put("options", List.of("True", "False"));
                answerData.put("booleanAnswer", candidate.getBooleanAnswer());
                answerData.put("correctOption", Boolean.TRUE.equals(candidate.getBooleanAnswer()) ? "A" : "B");
            }
            case FILL_BLANK -> answerData.put("acceptedAnswers", candidate.getAcceptedAnswers());
            case NUMERIC_RANGE -> {
                answerData.put("minValue", candidate.getMinValue());
                answerData.put("maxValue", candidate.getMaxValue());
                answerData.put("unit", candidate.getUnit());
                answerData.put("tolerance", candidate.getTolerance());
            }
            case SHORT_ANSWER -> {
                answerData.put("rubricKeywords", candidate.getRubricKeywords());
                answerData.put("modelAnswer", candidate.getModelAnswer());
            }
        }

        try {
            return objectMapper.writeValueAsString(answerData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize answer data", e);
        }
    }

    private Map<String, Object> toResponse(Question question) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", question.getId());
        response.put("topic", question.getTopic());
        response.put("questionType", question.getQuestionType());
        response.put("questionText", question.getQuestionText());
        response.put("complexity", question.getComplexity());
        response.put("reviewStatus", question.getReviewStatus());
        response.put("explanation", question.getExplanation());
        response.put("answerData", readAnswerData(question.getAnswerData()));
        response.put("createdAt", question.getCreatedAt());
        response.put("reviewedAt", question.getReviewedAt());
        response.put("reviewedBy", question.getReviewedBy());
        return response;
    }

    private Object readAnswerData(String answerData) {
        try {
            return objectMapper.readValue(answerData, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String buildQuestionHash(QuestionType questionType, String questionText) {
        String normalized = questionType.name() + ":" + questionText.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return sha256(normalized);
    }

    private String sha256(String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
