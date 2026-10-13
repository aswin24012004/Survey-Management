package com.healthcare.survey.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyTemplateJsonWrapper {

    @JsonProperty("template")
    private SurveyTemplateRequest template;

    public SurveyTemplateJsonWrapper() {}

    public SurveyTemplateJsonWrapper(SurveyTemplateRequest template) {
        this.template = template;
    }

    public SurveyTemplateRequest getTemplate() {
        return template;
    }

    public void setTemplate(SurveyTemplateRequest template) {
        this.template = template;
    }
}
