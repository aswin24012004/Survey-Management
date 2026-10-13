package com.healthcare.survey.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionAnswerDto {

    @NotBlank(message = "Question ID is required")
    @JsonProperty("questionId")
    private String questionId;

    @NotNull(message = "Answer value is required")
    @JsonProperty("value")
    private Object value;

    public SubmissionAnswerDto() {}

    public SubmissionAnswerDto(String questionId, Object value) {
        this.questionId = questionId;
        this.value = value;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
