package com.exam.aiquestions.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.exam.aiquestions.model.QuestionComplexity;
import com.exam.aiquestions.model.QuestionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LLMService {
    public static final int QUESTION_COUNT = 5;

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<QuestionCandidate> generateQuestions(String context, String topic, QuestionType questionType) {

        try {

            String prompt = buildPrompt(context, topic, questionType);

            RestTemplate restTemplate = new RestTemplate();
            OllamaGenerateRequest request = new OllamaGenerateRequest("llama3", prompt, false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OllamaGenerateRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<OllamaGenerateResponse> response =
                    restTemplate.postForEntity(OLLAMA_URL, entity, OllamaGenerateResponse.class);

            String rawResponse = response.getBody() != null ? response.getBody().response() : "[]";
            return parseQuestions(rawResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of();
    }

    private String buildPrompt(String context, String topic, QuestionType questionType) {
        return """
You are an assessment author creating high-quality, academically correct questions for an edtech question bank.

Generate exactly %s unique %s questions using only the provided source content.
Return only valid JSON. Do not add markdown, code fences, or any extra text.
Each JSON object must contain:
- questionText
- complexity
- explanation
- options
- correctOption
- acceptedAnswers
- booleanAnswer
- minValue
- maxValue
- unit
- tolerance
- rubricKeywords
- modelAnswer

Common rules:
- complexity must be one of EASY, MEDIUM, HARD
- explanation should briefly justify the answer
- Keep the questions distinct from each other
- Use only the provided content
- Do not invent facts that are not supported by the source content
- Prefer academic phrasing over generic motivational or conversational phrasing
- Ground every question in the source content's terminology, definitions, facts, formulas, procedures, or examples
- If the source content is insufficient for a good question, return fewer questions rather than guessing
- Avoid vague stems unless that exact idea is clearly supported by the source
- Favor curriculum-style questions that test understanding of the uploaded PDF, not broad world knowledge

Type-specific rules:
%s

Expected JSON example:
[
  {
    "questionText": "string",
    "complexity": "MEDIUM",
    "explanation": "string",
    "options": ["A", "B", "C", "D"],
    "correctOption": "A",
    "acceptedAnswers": ["string"],
    "booleanAnswer": true,
    "minValue": 10,
    "maxValue": 12,
    "unit": "kg",
    "tolerance": 0.5,
    "rubricKeywords": ["keyword1", "keyword2"],
    "modelAnswer": "string"
  }
]

Topic: %s

Source Content:
%s
""".formatted(
                QUESTION_COUNT,
                questionType.name(),
                instructionsFor(questionType),
                topic,
                context);
    }

    private String instructionsFor(QuestionType questionType) {
        return switch (questionType) {
            case MCQ -> """
- options must contain exactly 4 choices
- correctOption must be A, B, C, or D
- For academic content, make distractors plausible and close to the source material
- acceptedAnswers, booleanAnswer, minValue, maxValue, unit, tolerance, rubricKeywords, and modelAnswer must be null or empty
""";
            case TRUE_FALSE -> """
- options should be ["True", "False"]
- booleanAnswer must be true or false
- correctOption must be "A" for True and "B" for False
- acceptedAnswers, minValue, maxValue, unit, tolerance, rubricKeywords, and modelAnswer must be null or empty
""";
            case FILL_BLANK -> """
- questionText must contain a blank represented by five underscores: _____
- acceptedAnswers must contain one or more acceptable text answers
- Create the blank from a key academic term, phrase, value, formula component, or definition present in the source content
- Do not create generic sentences that could apply to any chapter; tie the blank to the uploaded content
- options, correctOption, booleanAnswer, minValue, maxValue, unit, tolerance, rubricKeywords, and modelAnswer must be null or empty
""";
            case NUMERIC_RANGE -> """
- minValue and maxValue must be numbers
- tolerance may be null or a non-negative number
- unit may be provided when relevant
- options, correctOption, acceptedAnswers, booleanAnswer, rubricKeywords, and modelAnswer must be null or empty
""";
            case SHORT_ANSWER -> """
- rubricKeywords must contain important expected keywords
- modelAnswer must provide a concise ideal answer
- Prefer concept explanations, definitions, procedural reasoning, or formula interpretation based on the source
- options, correctOption, acceptedAnswers, booleanAnswer, minValue, maxValue, unit, and tolerance must be null or empty
""";
        };
    }

    private List<QuestionCandidate> parseQuestions(String rawResponse) throws JsonProcessingException {
        String cleanedResponse = rawResponse == null ? "[]" : rawResponse.trim();
        if (cleanedResponse.startsWith("```")) {
            cleanedResponse = cleanedResponse.replaceFirst("^```(?:json)?\\s*", "");
            cleanedResponse = cleanedResponse.replaceFirst("\\s*```$", "");
        }

        int arrayStart = cleanedResponse.indexOf('[');
        int arrayEnd = cleanedResponse.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            cleanedResponse = cleanedResponse.substring(arrayStart, arrayEnd + 1);
        }

        return objectMapper.readValue(cleanedResponse, new TypeReference<List<QuestionCandidate>>() {
        });
    }

    public static class QuestionCandidate {
        private String questionText;
        private QuestionComplexity complexity;
        private String explanation;
        private List<String> options;
        private String correctOption;
        private List<String> acceptedAnswers;
        private Boolean booleanAnswer;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private String unit;
        private BigDecimal tolerance;
        private List<String> rubricKeywords;
        private String modelAnswer;

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public QuestionComplexity getComplexity() { return complexity; }
        public void setComplexity(QuestionComplexity complexity) { this.complexity = complexity; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public String getCorrectOption() { return correctOption; }
        public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
        public List<String> getAcceptedAnswers() { return acceptedAnswers; }
        public void setAcceptedAnswers(List<String> acceptedAnswers) { this.acceptedAnswers = acceptedAnswers; }
        public Boolean getBooleanAnswer() { return booleanAnswer; }
        public void setBooleanAnswer(Boolean booleanAnswer) { this.booleanAnswer = booleanAnswer; }
        public BigDecimal getMinValue() { return minValue; }
        public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
        public BigDecimal getMaxValue() { return maxValue; }
        public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getTolerance() { return tolerance; }
        public void setTolerance(BigDecimal tolerance) { this.tolerance = tolerance; }
        public List<String> getRubricKeywords() { return rubricKeywords; }
        public void setRubricKeywords(List<String> rubricKeywords) { this.rubricKeywords = rubricKeywords; }
        public String getModelAnswer() { return modelAnswer; }
        public void setModelAnswer(String modelAnswer) { this.modelAnswer = modelAnswer; }
    }

    private record OllamaGenerateRequest(String model, String prompt, boolean stream) {
    }

    private record OllamaGenerateResponse(String response) {
    }
}
