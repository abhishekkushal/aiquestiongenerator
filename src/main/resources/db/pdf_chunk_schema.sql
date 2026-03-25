ALTER TABLE pdf_chunks
    ADD COLUMN IF NOT EXISTS domain VARCHAR(100),
    ADD COLUMN IF NOT EXISTS subject VARCHAR(150),
    ADD COLUMN IF NOT EXISTS topic VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role_or_exam VARCHAR(150),
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_pdf_chunks_domain ON pdf_chunks (domain);
CREATE INDEX IF NOT EXISTS idx_pdf_chunks_subject ON pdf_chunks (subject);
CREATE INDEX IF NOT EXISTS idx_pdf_chunks_topic ON pdf_chunks (topic);
