package com.healthcare.survey.dto;

import com.healthcare.survey.entity.ResponseStatus;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class SurveyResponseDto {
    private Long id;
    private String referenceId;
    private Long surveyLinkId;
    private String respondentName;
    private String mobileNumber;
    private String emailAddress;
    private Integer overallRating;
    private String overallComments;
    private ResponseStatus status;
    private OffsetDateTime submittedAt;
    private Map<String, Object> answers = new HashMap<>();

    public SurveyResponseDto() {}

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

    public Long getSurveyLinkId() {
        return surveyLinkId;
    }

    public void setSurveyLinkId(Long surveyLinkId) {
        this.surveyLinkId = surveyLinkId;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public String getOverallComments() {
        return overallComments;
    }

    public void setOverallComments(String overallComments) {
        this.overallComments = overallComments;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Map<String, Object> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Object> answers) {
        this.answers = answers;
    }
}
