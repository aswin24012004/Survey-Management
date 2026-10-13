package com.healthcare.survey.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "survey_response")
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_id", nullable = false, unique = true, length = 64)
    private String referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_link_id", nullable = false)
    private SurveyLink surveyLink;

    @Column(name = "respondent_name", length = 255)
    private String respondentName;

    @Column(name = "mobile_number", length = 50)
    private String mobileNumber;

    @Column(name = "email_address", length = 255)
    private String emailAddress;

    @Column(name = "overall_rating")
    private Integer overallRating;

    @Column(name = "overall_comments", columnDefinition = "TEXT")
    private String overallComments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResponseStatus status = ResponseStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SurveyResponseAnswer> answers = new ArrayList<>();

    public SurveyResponse() {}

    @PrePersist
    public void onPrePersist() {
        if (this.submittedAt == null) {
            this.submittedAt = OffsetDateTime.now();
        }
        if (this.referenceId == null || this.referenceId.isBlank()) {
            this.referenceId = "RESP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.status == null) {
            this.status = ResponseStatus.SUBMITTED;
        }
    }

    public void addAnswer(SurveyResponseAnswer answer) {
        answers.add(answer);
        answer.setResponse(this);
    }

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

    public SurveyLink getSurveyLink() {
        return surveyLink;
    }

    public void setSurveyLink(SurveyLink surveyLink) {
        this.surveyLink = surveyLink;
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

    public List<SurveyResponseAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SurveyResponseAnswer> answers) {
        this.answers = answers;
    }
}
