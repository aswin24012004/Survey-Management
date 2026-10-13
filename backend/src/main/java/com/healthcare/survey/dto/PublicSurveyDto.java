package com.healthcare.survey.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PublicSurveyDto {
    private String token;
    private String referenceId;
    private String name;
    private String description;
    private String category;
    private Integer versionNumber;
    private OffsetDateTime expiresAt;
    private Integer maxResponses;
    private Boolean active;
    private List<QuestionDefinitionDto> questions = new ArrayList<>();

    public PublicSurveyDto() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getMaxResponses() {
        return maxResponses;
    }

    public void setMaxResponses(Integer maxResponses) {
        this.maxResponses = maxResponses;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<QuestionDefinitionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDefinitionDto> questions) {
        this.questions = questions;
    }
}
