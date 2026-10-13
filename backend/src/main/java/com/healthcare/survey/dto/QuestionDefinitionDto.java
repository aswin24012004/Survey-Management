package com.healthcare.survey.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.survey.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionDefinitionDto {

    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Question text is required")
    @JsonProperty("text")
    private String text;

    @NotNull(message = "Question type is required")
    @JsonProperty("type")
    private QuestionType type;

    @JsonProperty("required")
    private Boolean required = false;

    @JsonProperty("displayOrder")
    private Integer displayOrder = 0;

    @JsonProperty("min")
    private Integer min;

    @JsonProperty("max")
    private Integer max;

    @JsonProperty("options")
    private List<String> options = new ArrayList<>();

    @JsonProperty("placeholder")
    private String placeholder;

    @JsonProperty("helpText")
    private String helpText;

    public QuestionDefinitionDto() {}

    public QuestionDefinitionDto(String id, String text, QuestionType type, Boolean required) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.required = required;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required != null ? required : false;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getDisplayOrder() {
        return displayOrder != null ? displayOrder : 0;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public List<String> getOptions() {
        return options != null ? options : new ArrayList<>();
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }
}
