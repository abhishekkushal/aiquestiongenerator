package com.exam.aiquestions.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private LLMService llmService;

    public String generateMCQ(String topic) {

        List<String> chunks = vectorSearchService.search(topic);

        String context = String.join("\n", chunks);

        return llmService.generateQuestions(context, topic);
    }
}