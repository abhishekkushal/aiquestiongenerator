package com.exam.aiquestions.service;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.exam.aiquestions.repository.PdfChunkRepository;
import com.pgvector.PGvector;

@Service
public class PDFService {
    private static final Pattern PRIVATE_USE_CHARS = Pattern.compile("[\\p{Co}]");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");
    private static final Pattern JUNK_SYMBOLS = Pattern.compile("[\\u2192\\u25A0\\u25A1\\uFFFD]+");
    private static final Pattern HORIZONTAL_WHITESPACE = Pattern.compile("[\\t\\x0B\\f\\r ]+");
    private static final Pattern EXCESSIVE_NEWLINES = Pattern.compile("\\n{3,}");

    @Autowired
    EmbeddingService embeddingService;

    @Autowired
    PdfChunkRepository repository;

    public void processPdf(MultipartFile file) {

        Tika tika = new Tika();

        try {

            String text = normalizeExtractedText(tika.parseToString(file.getInputStream()));

            List<String> chunks = splitText(text);

            for (String chunk : chunks) {
                if (chunk.isBlank()) {
                    continue;
                }

                PGvector embedding = embeddingService.generateEmbedding(chunk);
                repository.insertChunk(file.getOriginalFilename(), chunk, embedding.toString());
            }

        } catch (IOException | TikaException e) {
            e.printStackTrace();
        }
    }

    private String normalizeExtractedText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC);
        normalized = normalized.replace('\u00A0', ' ');
        normalized = PRIVATE_USE_CHARS.matcher(normalized).replaceAll(" ");
        normalized = CONTROL_CHARS.matcher(normalized).replaceAll(" ");
        normalized = JUNK_SYMBOLS.matcher(normalized).replaceAll(" ");
        normalized = normalized.replace("\r\n", "\n").replace('\r', '\n');
        normalized = HORIZONTAL_WHITESPACE.matcher(normalized).replaceAll(" ");
        normalized = normalized.replaceAll(" *\\n *", "\n");
        normalized = EXCESSIVE_NEWLINES.matcher(normalized).replaceAll("\n\n");
        return normalized.trim();
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
