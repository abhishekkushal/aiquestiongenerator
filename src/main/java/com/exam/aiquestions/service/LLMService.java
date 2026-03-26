package com.exam.aiquestions.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LLMService {

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public String generateQuestions(String context, String topic) {

        try {

            String prompt = """
You are a teacher.

Using the syllabus content below, generate 5 MCQ questions.

Topic: %s

Content:
%s

Format:

Q1:
A)
B)
C)
D)
Answer:

Q2:
...
""".formatted(topic, context);

            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> request = new HashMap<>();
            request.put("model", "llama3");
            request.put("prompt", prompt);
            request.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OLLAMA_URL, entity, Map.class);

            return (String) response.getBody().get("response");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error generating questions";
    }

    public String extractTopics(String content) {

        try {

            String prompt = """
Extract the main topics and subtopics from the following content.
Return them as a simple comma-separated list (e.g., "Topic1, Topic2, Topic3").
Only return the list, no other text.

Content:
%s
""".formatted(content);

            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> request = new HashMap<>();
            request.put("model", "llama3");
            request.put("prompt", prompt);
            request.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OLLAMA_URL, entity, Map.class);

            return (String) response.getBody().get("response");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}