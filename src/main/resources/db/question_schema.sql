CREATE TABLE IF NOT EXISTS questions (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(100) NOT NULL,
    subject VARCHAR(150),
    topic VARCHAR(255) NOT NULL,
    role_or_exam VARCHAR(150),
    question_type VARCHAR(30) NOT NULL CHECK (question_type IN ('MCQ', 'TRUE_FALSE', 'FILL_BLANK', 'NUMERIC_RANGE', 'SHORT_ANSWER')),
    question_text TEXT NOT NULL,
    complexity VARCHAR(20) NOT NULL CHECK (complexity IN ('EASY', 'MEDIUM', 'HARD')),
    review_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (review_status IN ('DRAFT', 'REVIEWED', 'APPROVED', 'REJECTED')),
    answer_data TEXT NOT NULL,
    explanation TEXT,
    question_hash VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_questions_topic ON questions (topic);
CREATE INDEX IF NOT EXISTS idx_questions_status ON questions (review_status);
