package com.healthcare.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.*;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.exception.InvalidSurveyException;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.SurveyLinkRepository;
import com.healthcare.survey.repository.SurveyResponseRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import com.healthcare.survey.repository.SurveyVersionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SurveyTemplateService {

    private final SurveyTemplateRepository templateRepository;
    private final SurveyLinkRepository linkRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyVersionRepository versionRepository;
    private final AppUserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Value("${app.public-survey.base-url:http://localhost:5173/survey}")
    private String publicSurveyBaseUrl;

    public SurveyTemplateService(SurveyTemplateRepository templateRepository,
                                 SurveyLinkRepository linkRepository,
                                 SurveyResponseRepository responseRepository,
                                 SurveyVersionRepository versionRepository,
                                 AppUserRepository userRepository,
                                 AuditLogService auditLogService,
                                 ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.linkRepository = linkRepository;
        this.responseRepository = responseRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SurveyTemplateResponse createTemplate(SurveyTemplateRequest request, String username) {
        validateTemplateRequest(request);

        AppUser adminUser = userRepository.findByUsername(username).orElse(null);

        SurveyTemplate template = new SurveyTemplate();
        template.setName(request.getName().trim());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory() != null ? request.getCategory().trim() : "GENERAL");
        template.setStatus(SurveyStatus.DRAFT);
        template.setCreatedBy(adminUser);

        populateQuestions(template, request.getQuestions());

        SurveyTemplate saved = templateRepository.save(template);
        auditLogService.logAction(username, "SURVEY_CREATED", "SURVEY_TEMPLATE", saved.getId().toString(), "Created survey: " + saved.getName());

        return toTemplateResponse(saved);
    }

    @Transactional
    public SurveyTemplateResponse updateTemplate(Long id, SurveyTemplateRequest request, String username) {
        SurveyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with ID: " + id));

        if (template.getStatus() == SurveyStatus.ARCHIVED) {
            throw new InvalidSurveyException("Cannot update an archived survey template");
        }

        validateTemplateRequest(request);

        template.setName(request.getName().trim());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory() != null ? request.getCategory().trim() : "GENERAL");

        template.clearQuestions();
        populateQuestions(template, request.getQuestions());

        SurveyTemplate saved = templateRepository.save(template);
        auditLogService.logAction(username, "SURVEY_EDITED", "SURVEY_TEMPLATE", saved.getId().toString(), "Updated survey: " + saved.getName());

        return toTemplateResponse(saved);
    }

    @Transactional(readOnly = true)
    public SurveyTemplateResponse getTemplateById(Long id) {
        SurveyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with ID: " + id));
        return toTemplateResponse(template);
    }

    @Transactional(readOnly = true)
    public List<SurveySummaryResponse> getAllTemplates() {
        return templateRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void archiveTemplate(Long id, String username) {
        SurveyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with ID: " + id));

        template.setStatus(SurveyStatus.ARCHIVED);
        templateRepository.save(template);

        // Deactivate active links for this template
        linkRepository.findTopBySurveyVersion_Template_IdAndActiveTrueOrderByCreatedAtDesc(id).ifPresent(link -> {
            link.setActive(false);
            linkRepository.save(link);
        });

        auditLogService.logAction(username, "SURVEY_ARCHIVED", "SURVEY_TEMPLATE", id.toString(), "Archived survey: " + template.getName());
    }

    public void validateTemplateRequest(SurveyTemplateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidSurveyException("Survey template name is required");
        }
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new InvalidSurveyException("Survey template must contain at least one question");
        }

        Set<String> seenKeys = new HashSet<>();
        int index = 1;
        for (QuestionDefinitionDto q : request.getQuestions()) {
            if (q.getId() == null || q.getId().trim().isEmpty()) {
                q.setId("q" + index);
            }
            String key = q.getId().trim();
            if (seenKeys.contains(key)) {
                throw new InvalidSurveyException("Duplicate question ID/key found: " + key);
            }
            seenKeys.add(key);

            if (q.getText() == null || q.getText().trim().isEmpty()) {
                throw new InvalidSurveyException("Question text is required for question ID: " + key);
            }
            if (q.getType() == null) {
                throw new InvalidSurveyException("Question type is required for question ID: " + key);
            }

            // Question-type specific validations
            switch (q.getType()) {
                case RATING:
                    if (q.getMin() == null) q.setMin(1);
                    if (q.getMax() == null) q.setMax(5);
                    if (q.getMin() >= q.getMax()) {
                        throw new InvalidSurveyException("For rating question " + key + ", min (" + q.getMin() + ") must be less than max (" + q.getMax() + ")");
                    }
                    break;
                case SINGLE_CHOICE:
                case MULTIPLE_CHOICE:
                case DROPDOWN:
                    if (q.getOptions() == null || q.getOptions().size() < 2) {
                        throw new InvalidSurveyException("Choice question " + key + " requires at least 2 distinct options");
                    }
                    break;
                default:
                    break;
            }
            index++;
        }
    }

    private void populateQuestions(SurveyTemplate template, List<QuestionDefinitionDto> questionDtos) {
        int order = 1;
        for (QuestionDefinitionDto dto : questionDtos) {
            SurveyQuestion question = new SurveyQuestion();
            question.setQuestionKey(dto.getId().trim());
            question.setQuestionText(dto.getText().trim());
            question.setQuestionType(dto.getType());
            question.setRequired(Boolean.TRUE.equals(dto.getRequired()));
            question.setDisplayOrder(dto.getDisplayOrder() != null && dto.getDisplayOrder() > 0 ? dto.getDisplayOrder() : order);

            Map<String, Object> configMap = new HashMap<>();
            if (dto.getMin() != null) configMap.put("min", dto.getMin());
            if (dto.getMax() != null) configMap.put("max", dto.getMax());
            if (dto.getOptions() != null && !dto.getOptions().isEmpty()) configMap.put("options", dto.getOptions());
            if (dto.getPlaceholder() != null) configMap.put("placeholder", dto.getPlaceholder());
            if (dto.getHelpText() != null) configMap.put("helpText", dto.getHelpText());

            try {
                question.setConfigurationJson(objectMapper.writeValueAsString(configMap));
            } catch (JsonProcessingException e) {
                question.setConfigurationJson("{}");
            }

            template.addQuestion(question);
            order++;
        }
    }

    public SurveyTemplateResponse toTemplateResponse(SurveyTemplate template) {
        SurveyTemplateResponse res = new SurveyTemplateResponse();
        res.setId(template.getId());
        res.setReferenceId(template.getReferenceId());
        res.setName(template.getName());
        res.setDescription(template.getDescription());
        res.setCategory(template.getCategory());
        res.setStatus(template.getStatus());
        res.setCreatedAt(template.getCreatedAt());
        res.setUpdatedAt(template.getUpdatedAt());
        res.setCreatedByUsername(template.getCreatedBy() != null ? template.getCreatedBy().getUsername() : null);

        List<QuestionDefinitionDto> questionList = template.getQuestions().stream()
                .map(this::toQuestionDto)
                .collect(Collectors.toList());
        res.setQuestions(questionList);

        linkRepository.findTopBySurveyVersion_Template_IdAndActiveTrueOrderByCreatedAtDesc(template.getId())
                .ifPresent(link -> res.setActiveLink(toLinkResponse(link)));

        res.setVersionCount(versionRepository.findByTemplateIdOrderByVersionNumberDesc(template.getId()).size());
        res.setResponseCount(responseRepository.countBySurveyLink_SurveyVersion_Template_Id(template.getId()));

        return res;
    }

    public SurveySummaryResponse toSummaryResponse(SurveyTemplate template) {
        SurveySummaryResponse res = new SurveySummaryResponse();
        res.setId(template.getId());
        res.setReferenceId(template.getReferenceId());
        res.setName(template.getName());
        res.setDescription(template.getDescription());
        res.setCategory(template.getCategory());
        res.setStatus(template.getStatus());
        res.setCreatedAt(template.getCreatedAt());
        res.setUpdatedAt(template.getUpdatedAt());
        res.setQuestionCount(template.getQuestions().size());
        res.setResponseCount(responseRepository.countBySurveyLink_SurveyVersion_Template_Id(template.getId()));

        linkRepository.findTopBySurveyVersion_Template_IdAndActiveTrueOrderByCreatedAtDesc(template.getId()).ifPresent(link -> {
            res.setActiveLinkId(link.getId());
            res.setActiveLinkUrl(publicSurveyBaseUrl + "/" + link.getPublicToken());
            res.setActiveLinkToken(link.getPublicToken());
            res.setActiveLinkIsActive(link.getActive());
        });

        return res;
    }

    public QuestionDefinitionDto toQuestionDto(SurveyQuestion question) {
        QuestionDefinitionDto dto = new QuestionDefinitionDto();
        dto.setId(question.getQuestionKey());
        dto.setText(question.getQuestionText());
        dto.setType(question.getQuestionType());
        dto.setRequired(question.getRequired());
        dto.setDisplayOrder(question.getDisplayOrder());

        if (question.getConfigurationJson() != null && !question.getConfigurationJson().isBlank()) {
            try {
                Map<String, Object> config = objectMapper.readValue(question.getConfigurationJson(), new TypeReference<Map<String, Object>>() {});
                if (config.get("min") instanceof Number n) dto.setMin(n.intValue());
                if (config.get("max") instanceof Number n) dto.setMax(n.intValue());
                if (config.get("options") instanceof List<?> list) {
                    dto.setOptions(list.stream().map(Object::toString).collect(Collectors.toList()));
                }
                if (config.get("placeholder") instanceof String s) dto.setPlaceholder(s);
                if (config.get("helpText") instanceof String s) dto.setHelpText(s);
            } catch (JsonProcessingException ignored) {}
        }

        return dto;
    }

    public SurveyLinkResponse toLinkResponse(SurveyLink link) {
        SurveyLinkResponse res = new SurveyLinkResponse();
        res.setId(link.getId());
        res.setToken(link.getPublicToken());
        res.setSurveyUrl(publicSurveyBaseUrl + "/" + link.getPublicToken());
        res.setActive(link.getActive());
        res.setExpiresAt(link.getExpiresAt());
        res.setMaxResponses(link.getMaxResponses());
        res.setResponseCount(link.getResponseCount());
        res.setCreatedAt(link.getCreatedAt());
        res.setRevokedAt(link.getRevokedAt());
        if (link.getSurveyVersion() != null) {
            res.setVersionNumber(link.getSurveyVersion().getVersionNumber());
        }
        return res;
    }
}
