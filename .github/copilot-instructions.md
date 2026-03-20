# AIQuestions Spring Boot Application - Copilot Instructions

## Project Overview
This is a Spring Boot application for AI-powered question generation from PDF documents. It uses Retrieval-Augmented Generation (RAG) with vector embeddings stored in PostgreSQL (with pgvector extension) and Ollama for LLM services.

**Key Technologies:**
- Java 17, Spring Boot 3.x
- PostgreSQL with pgvector for vector storage
- Ollama for embeddings (nomic-embed-text) and LLM (llama3)
- Apache Tika for PDF parsing

## Build and Run Commands
- **Build**: `./mvnw clean install` (Linux/Mac) or `mvnw.cmd clean install` (Windows)
- **Run**: `./mvnw spring-boot:run`
- **Test**: `./mvnw test`
- **Package**: `./mvnw package`

**Prerequisites:**
- Java 17 installed
- PostgreSQL running with pgvector extension
- Ollama server running with required models

## Architecture and Component Boundaries
The application follows a 3-tier MVC pattern:

```
Controller → Service → Repository → PostgreSQL
```

**Main Flows:**
1. **PDF Upload**: Parse PDF → Chunk into 500-char pieces → Generate embeddings → Store in DB
2. **Question Generation**: Vector search for relevant chunks → LLM generates MCQs
3. **Search**: Embed query → Vector similarity search → Return results

**Key Components:**
- Controllers: REST endpoints for upload, generate, search
- Services: Business logic (PDF processing, embeddings, LLM, vector search)
- Repository: Data access with native SQL for vector operations
- Models: PdfChunk entity (Student.java is unused debug code)

## Project-Specific Conventions
- **Dependency Injection**: Field-level `@Autowired` (not constructor injection)
- **Configuration**: `@Value` annotations for properties; hardcoded fallbacks in code
- **Database**: `hibernate.ddl-auto: none` - manual schema management
- **Chunking**: Fixed 500-character chunks without overlap
- **Queries**: Native SQL with pgvector operators (`<->` for cosine similarity)
- **Error Handling**: Basic try-catch with printStackTrace()

## Potential Pitfalls and Environment Issues
- **Ollama Dependency**: App requires Ollama running on localhost:11434. Start with `ollama serve` and pull models: `nomic-embed-text`, `llama3`
- **Database Setup**: Ensure pgvector extension is installed (`CREATE EXTENSION vector;`). Table schema must include `embedding VECTOR(768)` column
- **Vector Casting**: Embeddings stored as string representations - verify dimension matching
- **Performance**: N+1 inserts for chunks; no batching; RestTemplate created per request
- **Timeouts**: No configured timeouts for external calls - can cause hangs
- **File Uploads**: 200MB limit; large PDFs may cause memory issues

## Key Files and Patterns
- `pom.xml`: Maven dependencies and build config
- `src/main/resources/application.yaml`: Configuration properties
- `PDFService.java`: Document parsing and chunking example
- `EmbeddingService.java`: REST client for Ollama API
- `VectorSearchService.java`: RAG orchestration pattern
- `PdfChunkRepository.java`: Vector database queries

## Development Environment Setup
1. Install Java 17
2. Set up PostgreSQL with pgvector
3. Install and start Ollama with required models
4. Create database and table manually (ddl-auto: none)
5. Run `./mvnw spring-boot:run`

## Common Debugging Commands
- Check Ollama: `curl http://localhost:11434/api/tags`
- Verify pgvector: `psql -c "SELECT * FROM pg_extension WHERE extname = 'vector';"`
- Test endpoints after startup</content>
<parameter name="filePath">d:\AWS\code\aiquestions\.github\copilot-instructions.md