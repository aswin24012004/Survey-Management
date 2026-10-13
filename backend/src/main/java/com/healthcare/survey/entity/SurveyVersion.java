package com.healthcare.survey.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "survey_version", uniqueConstraints = {
    @UniqueConstraint(name = "uk_template_version", columnNames = {"template_id", "version_number"})
})
public class SurveyVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private SurveyTemplate template;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "published_json", nullable = false, columnDefinition = "TEXT")
    private String publishedJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private AppUser publishedBy;

    @Column(name = "published_at", nullable = false, updatable = false)
    private OffsetDateTime publishedAt;

    public SurveyVersion() {}

    @PrePersist
    public void onPrePersist() {
        if (this.publishedAt == null) {
            this.publishedAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SurveyTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SurveyTemplate template) {
        this.template = template;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getPublishedJson() {
        return publishedJson;
    }

    public void setPublishedJson(String publishedJson) {
        this.publishedJson = publishedJson;
    }

    public AppUser getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(AppUser publishedBy) {
        this.publishedBy = publishedBy;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
