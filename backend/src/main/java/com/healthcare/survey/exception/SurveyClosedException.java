package com.healthcare.survey.exception;

public class SurveyClosedException extends RuntimeException {
    public SurveyClosedException(String message) {
        super(message);
    }
}
