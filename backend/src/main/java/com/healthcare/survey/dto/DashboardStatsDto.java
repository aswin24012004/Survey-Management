package com.healthcare.survey.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardStatsDto {
    private long totalSurveys;
    private long publishedSurveys;
    private long draftSurveys;
    private long activeLinks;
    private long totalResponses;
    private Double averageRating;
    private List<RecentSubmissionDto> recentSubmissions = new ArrayList<>();

    public DashboardStatsDto() {}

    public long getTotalSurveys() {
        return totalSurveys;
    }

    public void setTotalSurveys(long totalSurveys) {
        this.totalSurveys = totalSurveys;
    }

    public long getPublishedSurveys() {
        return publishedSurveys;
    }

    public void setPublishedSurveys(long publishedSurveys) {
        this.publishedSurveys = publishedSurveys;
    }

    public long getDraftSurveys() {
        return draftSurveys;
    }

    public void setDraftSurveys(long draftSurveys) {
        this.draftSurveys = draftSurveys;
    }

    public long getActiveLinks() {
        return activeLinks;
    }

    public void setActiveLinks(long activeLinks) {
        this.activeLinks = activeLinks;
    }

    public long getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(long totalResponses) {
        this.totalResponses = totalResponses;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public List<RecentSubmissionDto> getRecentSubmissions() {
        return recentSubmissions;
    }

    public void setRecentSubmissions(List<RecentSubmissionDto> recentSubmissions) {
        this.recentSubmissions = recentSubmissions;
    }
}
