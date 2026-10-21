-- JPA EnumType.STRING stores enum constant names. V2 used JSON-facing names.
UPDATE survey_question
SET question_type = UPPER(question_type)
WHERE question_type IN ('rating', 'single_choice', 'multiple_choice', 'short_text',
                        'long_text', 'dropdown', 'yes_no', 'date', 'number', 'boolean');
