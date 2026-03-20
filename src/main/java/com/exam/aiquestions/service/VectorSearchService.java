package com.exam.aiquestions.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exam.aiquestions.model.PdfChunk;
import com.exam.aiquestions.repository.PdfChunkRepository;
import com.pgvector.PGvector;

@Service
public class VectorSearchService {

    @Autowired
    private PdfChunkRepository repository;

    @Autowired
    private EmbeddingService embeddingService;

    public List<String> search(String topic) {


        PGvector queryEmbedding = embeddingService.generateEmbedding(topic);

        String vectorString = queryEmbedding.toString();

        List<PdfChunk> results =
                repository.searchSimilarChunks(vectorString, 5);

        return results.stream()
                .map(PdfChunk::getChunkText)
                .toList();
    }
}
