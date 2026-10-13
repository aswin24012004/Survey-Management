package com.healthcare.survey.dto;

import com.healthcare.survey.entity.SurveyStatus;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SurveyTemplateResponse {
    private Long id;
    private String referenceId;
    private String name;
    private String description;
    private String category;
    private SurveyStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdByUsername;
    private List<QuestionDefinitionDto> questions = new ArrayList<>();
    private SurveyLinkResponse activeLink;
    private Integer versionCount;
    private Long responseCount;

    public SurveyTemplateResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public SurveyStatus getStatus() {
        return status;
    }

    public void setStatus(SurveyStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public List<QuestionDefinitionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDefinitionDto> questions) {
        this.questions = questions;
    }

    public SurveyLinkResponse getActiveLink() {
        return activeLink;
    }

    public void setActiveLink(SurveyLinkResponse activeLink) {
        this.activeLink = activeLink;
    }

    public Integer getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(Integer versionCount) {
        this.versionCount = versionCount;
    }

    public Long getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(Long responseCount) {
        this.responseCount = responseCount;
    }
}
