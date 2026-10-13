package com.healthcare.survey.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "survey_link")
public class SurveyLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_version_id", nullable = false)
    private SurveyVersion surveyVersion;

    @Column(name = "public_token", nullable = false, unique = true, length = 255)
    private String publicToken;

    @Column(name = "public_token_hash", nullable = false, unique = true, length = 255)
    private String publicTokenHash;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "max_responses")
    private Integer maxResponses;

    @Column(name = "response_count", nullable = false)
    private Integer responseCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public SurveyLink() {}

    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.active == null) {
            this.active = true;
        }
        if (this.responseCount == null) {
            this.responseCount = 0;
        }
    }

    public boolean isExpired() {
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isLimitReached() {
        return maxResponses != null && maxResponses > 0 && responseCount >= maxResponses;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(active) && !isExpired() && !isLimitReached();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SurveyVersion getSurveyVersion() {
        return surveyVersion;
    }

    public void setSurveyVersion(SurveyVersion surveyVersion) {
        this.surveyVersion = surveyVersion;
    }

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public String getPublicTokenHash() {
        return publicTokenHash;
    }

    public void setPublicTokenHash(String publicTokenHash) {
        this.publicTokenHash = publicTokenHash;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public Integer getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(Integer responseCount) {
        this.responseCount = responseCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
