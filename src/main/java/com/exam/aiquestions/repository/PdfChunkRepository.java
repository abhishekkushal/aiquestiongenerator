package com.exam.aiquestions.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exam.aiquestions.model.PdfChunk;

import jakarta.transaction.Transactional;

@Repository
public interface PdfChunkRepository extends JpaRepository<PdfChunk, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO pdf_chunks (document_name, chunk_text, embedding)
        VALUES (:doc, :chunk, CAST(:embedding AS vector))
        """, nativeQuery = true)
    void insertChunk(
            @Param("doc") String documentName,
            @Param("chunk") String chunkText,
            @Param("embedding") String embedding
    );

    @Query(value = """
            SELECT *
            FROM pdf_chunks
            ORDER BY embedding <-> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<PdfChunk> searchSimilarChunks(
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);
}
