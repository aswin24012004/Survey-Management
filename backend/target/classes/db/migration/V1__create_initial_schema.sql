-- V1__create_initial_schema.sql

-- 1. App User Table
CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Survey Template Table
CREATE TABLE IF NOT EXISTS survey_template (
    id BIGSERIAL PRIMARY KEY,
    reference_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Survey Question Table
CREATE TABLE IF NOT EXISTS survey_question (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES survey_template(id) ON DELETE CASCADE,
    question_key VARCHAR(100) NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    configuration_json TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_template_question_key UNIQUE (template_id, question_key)
);

-- 4. Survey Version Table (Immutable Published Snapshot)
CREATE TABLE IF NOT EXISTS survey_version (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES survey_template(id),
    version_number INT NOT NULL,
    published_json TEXT NOT NULL,
    published_by BIGINT REFERENCES app_user(id),
    published_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_template_version UNIQUE (template_id, version_number)
);

-- 5. Survey Link Table
CREATE TABLE IF NOT EXISTS survey_link (
    id BIGSERIAL PRIMARY KEY,
    survey_version_id BIGINT NOT NULL REFERENCES survey_version(id),
    public_token VARCHAR(255) NOT NULL UNIQUE,
    public_token_hash VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP WITH TIME ZONE,
    max_responses INT,
    response_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITH TIME ZONE
);

-- 6. Survey Response Table
CREATE TABLE IF NOT EXISTS survey_response (
    id BIGSERIAL PRIMARY KEY,
    reference_id VARCHAR(64) NOT NULL UNIQUE,
    survey_link_id BIGINT NOT NULL REFERENCES survey_link(id),
    respondent_name VARCHAR(255),
    mobile_number VARCHAR(50),
    email_address VARCHAR(255),
    overall_rating INT,
    overall_comments TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Survey Response Answer Table
CREATE TABLE IF NOT EXISTS survey_response_answer (
    id BIGSERIAL PRIMARY KEY,
    response_id BIGINT NOT NULL REFERENCES survey_response(id) ON DELETE CASCADE,
    question_key VARCHAR(100) NOT NULL,
    answer_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Audit Log Table
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT REFERENCES app_user(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for high performance & query optimization
CREATE INDEX IF NOT EXISTS idx_template_status ON survey_template(status);
CREATE INDEX IF NOT EXISTS idx_question_template_order ON survey_question(template_id, display_order);
CREATE INDEX IF NOT EXISTS idx_link_token_hash ON survey_link(public_token_hash);
CREATE INDEX IF NOT EXISTS idx_link_active ON survey_link(active);
CREATE INDEX IF NOT EXISTS idx_response_link ON survey_response(survey_link_id);
CREATE INDEX IF NOT EXISTS idx_response_submitted ON survey_response(submitted_at);
CREATE INDEX IF NOT EXISTS idx_answer_response ON survey_response_answer(response_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);
