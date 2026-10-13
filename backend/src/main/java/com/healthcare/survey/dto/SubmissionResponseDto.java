package com.healthcare.survey.dto;

import java.time.OffsetDateTime;

public class SubmissionResponseDto {
    private String confirmationId;
    private String message;
    private OffsetDateTime submittedAt;

    public SubmissionResponseDto() {}

    public SubmissionResponseDto(String confirmationId, String message, OffsetDateTime submittedAt) {
        this.confirmationId = confirmationId;
        this.message = message;
        this.submittedAt = submittedAt;
    }

    public String getConfirmationId() {
        return confirmationId;
    }

    public void setConfirmationId(String confirmationId) {
        this.confirmationId = confirmationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
