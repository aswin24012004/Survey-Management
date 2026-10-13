package com.healthcare.survey.dto;

import java.time.OffsetDateTime;

public class PublishRequest {
    private OffsetDateTime expiresAt;
    private Integer maxResponses;
    private Boolean forceNewLink = false;

    public PublishRequest() {}

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

    public Boolean getForceNewLink() {
        return forceNewLink != null ? forceNewLink : false;
    }

    public void setForceNewLink(Boolean forceNewLink) {
        this.forceNewLink = forceNewLink;
    }
}
