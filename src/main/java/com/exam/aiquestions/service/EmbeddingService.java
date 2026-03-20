package com.exam.aiquestions.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pgvector.PGvector;

@Service
public class EmbeddingService {
	@Value("${ollama.embedding.url}")
	private String embeddingUrl;

    public PGvector generateEmbedding(String text) {

    	RestTemplate restTemplate = new RestTemplate();

        Map<String,Object> request = Map.of(
                "model","nomic-embed-text",
                "prompt",text
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(embeddingUrl, request, Map.class);

        List<Double> embeddingList = (List<Double>) response.getBody().get("embedding");

        float[] vector = new float[embeddingList.size()];

        for(int i=0;i<embeddingList.size();i++){
            vector[i] = embeddingList.get(i).floatValue();
        }

        return new PGvector(vector);
    }
}