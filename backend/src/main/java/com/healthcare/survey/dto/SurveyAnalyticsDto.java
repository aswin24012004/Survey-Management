package com.healthcare.survey.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyAnalyticsDto {
    private Long surveyId;
    private String surveyName;
    private long totalResponses;
    private Double averageRating;
    private List<QuestionStatDto> questionStats = new ArrayList<>();
    private Map<Integer, Long> ratingDistribution = new HashMap<>();
    private Map<String, Long> timeline = new HashMap<>();

    public SurveyAnalyticsDto() {}

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
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

    public List<QuestionStatDto> getQuestionStats() {
        return questionStats;
    }

    public void setQuestionStats(List<QuestionStatDto> questionStats) {
        this.questionStats = questionStats;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }

    public Map<String, Long> getTimeline() {
        return timeline;
    }

    public void setTimeline(Map<String, Long> timeline) {
        this.timeline = timeline;
    }
}
