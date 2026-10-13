package com.healthcare.survey.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.*;
import com.healthcare.survey.service.AuditLogService;
import com.healthcare.survey.service.SurveyAnalyticsService;
import com.healthcare.survey.service.SurveyPublishService;
import com.healthcare.survey.service.SurveyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Admin Survey Management", description = "Endpoints for administrators to manage templates, publishing, links, and analytics")
public class AdminSurveyController {

    private final SurveyTemplateService templateService;
    private final SurveyPublishService publishService;
    private final SurveyAnalyticsService analyticsService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AdminSurveyController(SurveyTemplateService templateService,
                                 SurveyPublishService publishService,
                                 SurveyAnalyticsService analyticsService,
                                 AuditLogService auditLogService,
                                 ObjectMapper objectMapper) {
        this.templateService = templateService;
        this.publishService = publishService;
        this.analyticsService = analyticsService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/surveys")
    @Operation(summary = "Create survey template")
    public ResponseEntity<SurveyTemplateResponse> createSurvey(
            @Valid @RequestBody SurveyTemplateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SurveyTemplateResponse response = templateService.createTemplate(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/surveys/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Import survey template from JSON", description = "Supports either raw survey object or standard wrapper format: {\"template\": { ... }}")
    public ResponseEntity<SurveyTemplateResponse> importSurvey(
            @RequestBody JsonNode rootNode,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        SurveyTemplateRequest templateRequest;
        if (rootNode.has("template")) {
            templateRequest = objectMapper.treeToValue(rootNode.get("template"), SurveyTemplateRequest.class);
        } else {
            templateRequest = objectMapper.treeToValue(rootNode, SurveyTemplateRequest.class);
        }

        SurveyTemplateResponse response = templateService.createTemplate(templateRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/surveys")
    @Operation(summary = "List all surveys with summary metrics")
    public ResponseEntity<List<SurveySummaryResponse>> getAllSurveys() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/surveys/{id}")
    @Operation(summary = "Get survey template by ID")
    public ResponseEntity<SurveyTemplateResponse> getSurveyById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @PutMapping("/surveys/{id}")
    @Operation(summary = "Update survey template")
    public ResponseEntity<SurveyTemplateResponse> updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody SurveyTemplateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request, userDetails.getUsername()));
    }

    @PostMapping("/surveys/{id}/publish")
    @Operation(summary = "Publish survey", description = "Creates an immutable snapshot version and active public link. Repeated clicks return active link.")
    public ResponseEntity<PublishResponse> publishSurvey(
            @PathVariable Long id,
            @RequestBody(required = false) PublishRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(publishService.publishSurvey(id, request, userDetails.getUsername()));
    }

    @PostMapping("/surveys/{id}/regenerate-link")
    @Operation(summary = "Regenerate public survey link", description = "Explicitly creates a new active public link and deactivates prior links.")
    public ResponseEntity<PublishResponse> regenerateLink(
            @PathVariable Long id,
            @RequestBody(required = false) PublishRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(publishService.regenerateLink(id, request, userDetails.getUsername()));
    }

    @GetMapping("/surveys/{id}/links")
    @Operation(summary = "Get all links for a survey template")
    public ResponseEntity<List<SurveyLinkResponse>> getSurveyLinks(@PathVariable Long id) {
        return ResponseEntity.ok(publishService.getLinksForSurvey(id));
    }

    @PatchMapping("/survey-links/{id}/deactivate")
    @Operation(summary = "Deactivate public survey link")
    public ResponseEntity<SurveyLinkResponse> deactivateLink(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(publishService.deactivateLink(id, userDetails.getUsername()));
    }

    @PostMapping("/surveys/{id}/archive")
    @Operation(summary = "Archive survey template")
    public ResponseEntity<Void> archiveSurvey(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        templateService.archiveTemplate(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/surveys/{id}/responses")
    @Operation(summary = "Get all submitted responses for a survey")
    public ResponseEntity<List<SurveyResponseDto>> getSurveyResponses(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getSurveyResponses(id));
    }

    @GetMapping("/surveys/{id}/analytics")
    @Operation(summary = "Get detailed analytics and question statistics for a survey")
    public ResponseEntity<SurveyAnalyticsDto> getSurveyAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getSurveyAnalytics(id));
    }

    @GetMapping("/surveys/{id}/export")
    @Operation(summary = "Export survey responses to CSV")
    public ResponseEntity<byte[]> exportResponses(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] csvBytes = analyticsService.exportResponsesToCsv(id, userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"survey_responses_" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard high-level KPIs and recent activity")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs for compliance tracking")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getRecentLogs());
    }
}
