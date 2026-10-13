package com.healthcare.survey.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyTemplateRequest {

    @NotBlank(message = "Survey template name is required")
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category = "GENERAL";

    @NotEmpty(message = "At least one question is required")
    @Valid
    @JsonProperty("questions")
    private List<QuestionDefinitionDto> questions = new ArrayList<>();

    public SurveyTemplateRequest() {}

    public SurveyTemplateRequest(String name, String description, List<QuestionDefinitionDto> questions) {
        this.name = name;
        this.description = description;
        this.questions = questions;
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

    public List<QuestionDefinitionDto> getQuestions() {
        return questions != null ? questions : new ArrayList<>();
    }

    public void setQuestions(List<QuestionDefinitionDto> questions) {
        this.questions = questions;
    }
}
