package com.healthcare.survey.dto;

import com.healthcare.survey.entity.SurveyStatus;
import java.time.OffsetDateTime;

public class SurveySummaryResponse {
    private Long id;
    private String referenceId;
    private String name;
    private String description;
    private String category;
    private SurveyStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int questionCount;
    private long responseCount;
    private Long activeLinkId;
    private String activeLinkUrl;
    private String activeLinkToken;
    private Boolean activeLinkIsActive;

    public SurveySummaryResponse() {}

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

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public long getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(long responseCount) {
        this.responseCount = responseCount;
    }

    public Long getActiveLinkId() {
        return activeLinkId;
    }

    public void setActiveLinkId(Long activeLinkId) {
        this.activeLinkId = activeLinkId;
    }

    public String getActiveLinkUrl() {
        return activeLinkUrl;
    }

    public void setActiveLinkUrl(String activeLinkUrl) {
        this.activeLinkUrl = activeLinkUrl;
    }

    public String getActiveLinkToken() {
        return activeLinkToken;
    }

    public void setActiveLinkToken(String activeLinkToken) {
        this.activeLinkToken = activeLinkToken;
    }

    public Boolean getActiveLinkIsActive() {
        return activeLinkIsActive;
    }

    public void setActiveLinkIsActive(Boolean activeLinkIsActive) {
        this.activeLinkIsActive = activeLinkIsActive;
    }
}
