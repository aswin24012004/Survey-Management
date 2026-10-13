package com.healthcare.survey.controller;

import com.healthcare.survey.dto.PublicSurveyDto;
import com.healthcare.survey.dto.SubmissionResponseDto;
import com.healthcare.survey.dto.SurveySubmissionRequest;
import com.healthcare.survey.service.PublicSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public Survey", description = "Public respondent survey viewing and submission endpoints (no authentication required)")
public class PublicSurveyController {

    private final PublicSurveyService publicSurveyService;

    public PublicSurveyController(PublicSurveyService publicSurveyService) {
        this.publicSurveyService = publicSurveyService;
    }

    @GetMapping("/surveys/{token}")
    @Operation(summary = "Get published survey by public token", description = "Loads dynamic survey questions and metadata without requiring user login")
    public ResponseEntity<PublicSurveyDto> getPublicSurvey(@PathVariable String token) {
        PublicSurveyDto survey = publicSurveyService.getPublicSurvey(token);
        return ResponseEntity.ok(survey);
    }

    @PostMapping("/surveys/{token}/responses")
    @Operation(summary = "Submit survey response", description = "Validates and saves respondent information and question answers in a single database transaction")
    public ResponseEntity<SubmissionResponseDto> submitSurveyResponse(
            @PathVariable String token,
            @Valid @RequestBody SurveySubmissionRequest request) {
        SubmissionResponseDto response = publicSurveyService.submitSurveyResponse(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
