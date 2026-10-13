package com.healthcare.survey.dto;

import java.time.OffsetDateTime;

public class RecentSubmissionDto {
    private String referenceId;
    private String surveyName;
    private String respondentName;
    private Integer overallRating;
    private OffsetDateTime submittedAt;

    public RecentSubmissionDto() {}

    public RecentSubmissionDto(String referenceId, String surveyName, String respondentName, Integer overallRating, OffsetDateTime submittedAt) {
        this.referenceId = referenceId;
        this.surveyName = surveyName;
        this.respondentName = respondentName;
        this.overallRating = overallRating;
        this.submittedAt = submittedAt;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
