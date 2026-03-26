package com.exam.aiquestions.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.exam.aiquestions.model.PdfChunk;
import com.exam.aiquestions.model.Topic;
import com.exam.aiquestions.repository.PdfChunkRepository;
import com.exam.aiquestions.repository.TopicRepository;
import com.pgvector.PGvector;

@Service
public class PDFService {

    @Autowired
    EmbeddingService embeddingService;

    @Autowired
    PdfChunkRepository repository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    LLMService llmService;

    public void processPdf(MultipartFile file) {

        Tika tika = new Tika();

        try {

            String text = tika.parseToString(file.getInputStream());

            List<String> chunks = splitText(text);
            List<PdfChunk> entities = new ArrayList<>();

            for (String chunk : chunks) {

                PGvector embedding = embeddingService.generateEmbedding(chunk);
                repository.insertChunk(
                        file.getOriginalFilename(),
                        chunk,embedding.toString()
                );

            }

            // Extract topics from the PDF content
            extractAndSaveTopics(text, file.getOriginalFilename());

        } catch (IOException | TikaException e) {
            e.printStackTrace();
        }
    }

    private void extractAndSaveTopics(String content, String documentName) {
        try {
            String topicsString = llmService.extractTopics(content);

            if (topicsString != null && !topicsString.trim().isEmpty()) {
                String[] topicNames = topicsString.split(",");

                for (String topicName : topicNames) {
                    String cleanedName = topicName.trim();

                    if (!cleanedName.isEmpty()) {
                        Topic topic = new Topic();
                        topic.setName(cleanedName);
                        topic.setDocumentName(documentName);
                        topicRepository.save(topic);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> splitText(String text) {

        int chunkSize = 500;

        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += chunkSize) {

            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));

        }

        return chunks;
    }
}