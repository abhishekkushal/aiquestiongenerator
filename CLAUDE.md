# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./mvnw clean install          # Linux/Mac
mvnw.cmd clean install        # Windows

# Run
./mvnw spring-boot:run

# Test
./mvnw test

# Run a single test class
./mvnw test -Dtest=AiquestionsApplicationTests

# Package
./mvnw package
```

## Prerequisites

- **Java 17**
- **PostgreSQL** with pgvector extension enabled:
  ```sql
  CREATE EXTENSION vector;
  -- Table must have: embedding VECTOR(768) column
  ```
- **Ollama** running on `localhost:11434` with models pulled:
  ```bash
  ollama serve
  ollama pull nomic-embed-text   # embeddings (768-dim)
  ollama pull llama3             # question generation
  ```
- Database `ai_exam` on PostgreSQL (credentials: `postgres/postgres`)
- Schema is managed manually (`ddl-auto: none`) — no auto-migration on startup

## Architecture

RAG pipeline: PDF upload → chunk → embed → store → query → generate MCQs

```
Controller → Service → Repository → PostgreSQL (pgvector)
                  ↕
              Ollama (localhost:11434)
```

**PDF Upload flow** (`PDFController` → `PDFService` → `EmbeddingService` → `PdfChunkRepository`):
1. Tika parses the PDF to plain text
2. Text is split into fixed 500-character chunks (no overlap)
3. Each chunk is embedded via Ollama (`nomic-embed-text`) → `PGvector` (768-dim float[])
4. Chunks inserted one-by-one via native SQL with `CAST(:embedding AS vector)`

**Question Generation flow** (`QuestionController` → `QuestionService` → `VectorSearchService` → `LLMService`):
1. Topic string is embedded and used for cosine similarity search (`<->` operator)
2. Top 5 nearest chunks retrieved as context
3. Context passed to Ollama (`llama3`) with MCQ prompt

**Search flow** (`SearchController` → `VectorSearchService`): same embedding + similarity search, returns raw chunk text

## Key Conventions

- **DI style**: Field-level `@Autowired` throughout (not constructor injection)
- **Config**: `@Value` for Ollama URLs; credentials hardcoded in `application.yaml`
- **Vector passing**: `PGvector.toString()` produces the string representation used in native queries; cast back with `CAST(:param AS vector)`
- **`Student.java`**: Unused model — debug artifact, not part of the main flow
- **`RestTemplate`**: Instantiated per-request inside `EmbeddingService` (no shared bean)

## Debugging

```bash
# Verify Ollama is running and models are available
curl http://localhost:11434/api/tags

# Verify pgvector extension
psql -U postgres -d ai_exam -c "SELECT * FROM pg_extension WHERE extname = 'vector';"
```
