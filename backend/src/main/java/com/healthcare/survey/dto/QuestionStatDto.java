package com.healthcare.survey.dto;

import com.healthcare.survey.entity.QuestionType;
import java.util.HashMap;
import java.util.Map;

public class QuestionStatDto {
    private String questionId;
    private String questionText;
    private QuestionType questionType;
    private long totalAnswers;
    private Double averageRating;
    private Map<String, Long> choiceDistribution = new HashMap<>();

    public QuestionStatDto() {}

    public QuestionStatDto(String questionId, String questionText, QuestionType questionType) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public long getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(long totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Map<String, Long> getChoiceDistribution() {
        return choiceDistribution;
    }

    public void setChoiceDistribution(Map<String, Long> choiceDistribution) {
        this.choiceDistribution = choiceDistribution;
    }
}
