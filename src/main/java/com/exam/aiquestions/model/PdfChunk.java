package com.exam.aiquestions.model;

import com.pgvector.PGvector;

import jakarta.persistence.*;

@Entity
@Table(name = "pdf_chunks")
public class PdfChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentName;

    @Column(columnDefinition = "TEXT")
    private String chunkText;

  //  @Column(columnDefinition = "vector(768)")
  //  private PGvector embedding;

    public Long getId() { return id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }

 //   public PGvector getEmbedding() { return embedding; }
 //   public void setEmbedding(PGvector embedding) { this.embedding = embedding; }
}