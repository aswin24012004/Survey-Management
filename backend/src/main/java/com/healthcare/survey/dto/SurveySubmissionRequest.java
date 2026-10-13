package com.healthcare.survey.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveySubmissionRequest {

    @Valid
    @JsonProperty("respondent")
    private RespondentDetailsDto respondent;

    @Valid
    @JsonProperty("answers")
    private List<SubmissionAnswerDto> answers = new ArrayList<>();

    public SurveySubmissionRequest() {}

    public SurveySubmissionRequest(RespondentDetailsDto respondent, List<SubmissionAnswerDto> answers) {
        this.respondent = respondent;
        this.answers = answers;
    }

    public RespondentDetailsDto getRespondent() {
        return respondent;
    }

    public void setRespondent(RespondentDetailsDto respondent) {
        this.respondent = respondent;
    }

    public List<SubmissionAnswerDto> getAnswers() {
        return answers != null ? answers : new ArrayList<>();
    }

    public void setAnswers(List<SubmissionAnswerDto> answers) {
        this.answers = answers;
    }
}
