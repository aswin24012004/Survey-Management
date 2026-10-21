-- V2__seed_admin_and_sample_survey.sql

-- Insert Default Admin User
-- Password is 'Admin@123456' hashed with BCrypt
INSERT INTO app_user (id, username, email, password_hash, role, enabled, created_at, updated_at)
VALUES (
    1,
    'admin',
    'admin@qms.healthcare',
    '$2a$10$P2NrkJqT9g1bZc00j9V31eQvHjXQ7Z03t3jE3wN1XoE0jN8m4QkXe',
    'ROLE_ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- Insert Sample Hospital Patient Satisfaction Survey
INSERT INTO survey_template (id, reference_id, name, description, category, status, created_by, created_at, updated_at)
VALUES (
    1,
    'tmpl-patient-satisfaction-001',
    'Hospital Patient Satisfaction Survey',
    'Patient experience feedback to improve hospital outpatient and inpatient care quality.',
    'PATIENT_CARE',
    'DRAFT',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- Insert Questions for Sample Survey
INSERT INTO survey_question (template_id, question_key, question_text, question_type, required, display_order, configuration_json, created_at)
VALUES
(
    1,
    'q1',
    'How satisfied are you with the waiting time before your consultation?',
    'rating',
    true,
    1,
    '{"min":1,"max":5,"placeholder":"Select rating 1 to 5","helpText":"1 = Very Dissatisfied, 5 = Very Satisfied"}',
    CURRENT_TIMESTAMP
),
(
    1,
    'q2',
    'Did the doctor explain your medical condition and treatment clearly?',
    'single_choice',
    true,
    2,
    '{"options":["Yes","No","Partially"]}',
    CURRENT_TIMESTAMP
),
(
    1,
    'q3',
    'How would you rate the cleanliness of the hospital and patient rooms?',
    'rating',
    true,
    3,
    '{"min":1,"max":5,"placeholder":"Select rating 1 to 5"}',
    CURRENT_TIMESTAMP
),
(
    1,
    'q4',
    'Would you recommend our healthcare facility to family and friends?',
    'yes_no',
    true,
    4,
    '{}',
    CURRENT_TIMESTAMP
),
(
    1,
    'q5',
    'Please share any suggestions or compliments for our clinical and nursing staff.',
    'long_text',
    false,
    5,
    '{"placeholder":"Write your feedback here..."}',
    CURRENT_TIMESTAMP
);

-- Synchronize PostgreSQL sequence values with seeded primary keys
SELECT setval('app_user_id_seq', COALESCE((SELECT MAX(id) FROM app_user), 1), true);
SELECT setval('survey_template_id_seq', COALESCE((SELECT MAX(id) FROM survey_template), 1), true);
SELECT setval('survey_question_id_seq', COALESCE((SELECT MAX(id) FROM survey_question), 1), true);
