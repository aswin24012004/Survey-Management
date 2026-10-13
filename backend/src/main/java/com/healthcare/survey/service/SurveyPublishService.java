package com.healthcare.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.PublishRequest;
import com.healthcare.survey.dto.PublishResponse;
import com.healthcare.survey.dto.SurveyLinkResponse;
import com.healthcare.survey.dto.SurveyTemplateResponse;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.exception.InvalidSurveyException;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.SurveyLinkRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import com.healthcare.survey.repository.SurveyVersionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SurveyPublishService {

    private final SurveyTemplateRepository templateRepository;
    private final SurveyVersionRepository versionRepository;
    private final SurveyLinkRepository linkRepository;
    private final AppUserRepository userRepository;
    private final SurveyTemplateService templateService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.public-survey.base-url:http://localhost:5173/survey}")
    private String publicSurveyBaseUrl;

    public SurveyPublishService(SurveyTemplateRepository templateRepository,
                                SurveyVersionRepository versionRepository,
                                SurveyLinkRepository linkRepository,
                                AppUserRepository userRepository,
                                SurveyTemplateService templateService,
                                AuditLogService auditLogService,
                                ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.versionRepository = versionRepository;
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
        this.templateService = templateService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public synchronized PublishResponse publishSurvey(Long templateId, PublishRequest request, String username) {
        SurveyTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with ID: " + templateId));

        if (template.getStatus() == SurveyStatus.ARCHIVED) {
            throw new InvalidSurveyException("Cannot publish an archived survey template");
        }

        if (template.getQuestions() == null || template.getQuestions().isEmpty()) {
            throw new InvalidSurveyException("Survey template must contain at least one question before publishing");
        }

        boolean forceNewLink = request != null && Boolean.TRUE.equals(request.getForceNewLink());

        // Repeated clicking Publish returns existing active link by default
        if (!forceNewLink) {
            Optional<SurveyLink> existingActiveLink = linkRepository
                    .findTopBySurveyVersion_Template_IdAndActiveTrueOrderByCreatedAtDesc(templateId);

            if (existingActiveLink.isPresent()) {
                SurveyLink link = existingActiveLink.get();
                if (!link.isExpired() && !link.isLimitReached()) {
                    return toPublishResponse(link);
                }
            }
        }

        AppUser publisher = userRepository.findByUsername(username).orElse(null);

        // Calculate next version number
        int nextVersionNumber = versionRepository
                .findTopByTemplateIdOrderByVersionNumberDesc(templateId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        // Generate immutable snapshot of template and questions
        SurveyTemplateResponse snapshot = templateService.toTemplateResponse(template);
        String publishedJson;
        try {
            publishedJson = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new InvalidSurveyException("Failed to serialize survey version snapshot");
        }

        SurveyVersion version = new SurveyVersion();
        version.setTemplate(template);
        version.setVersionNumber(nextVersionNumber);
        version.setPublishedJson(publishedJson);
        version.setPublishedBy(publisher);
        SurveyVersion savedVersion = versionRepository.save(version);

        // Deactivate older active links if forcing a new link
        if (forceNewLink) {
            linkRepository.findBySurveyVersion_Template_IdOrderByCreatedAtDesc(templateId).forEach(l -> {
                if (Boolean.TRUE.equals(l.getActive())) {
                    l.setActive(false);
                    l.setRevokedAt(OffsetDateTime.now());
                    linkRepository.save(l);
                }
            });
        }

        // Generate cryptographically secure token
        String publicToken = generateSecureToken();
        String publicTokenHash = hashToken(publicToken);

        SurveyLink newLink = new SurveyLink();
        newLink.setSurveyVersion(savedVersion);
        newLink.setPublicToken(publicToken);
        newLink.setPublicTokenHash(publicTokenHash);
        newLink.setActive(true);
        if (request != null) {
            newLink.setExpiresAt(request.getExpiresAt());
            newLink.setMaxResponses(request.getMaxResponses());
        }
        newLink.setResponseCount(0);

        SurveyLink savedLink = linkRepository.save(newLink);

        template.setStatus(SurveyStatus.PUBLISHED);
        templateRepository.save(template);

        auditLogService.logAction(username, "SURVEY_PUBLISHED", "SURVEY_TEMPLATE", templateId.toString(),
                "Published version " + nextVersionNumber + " with public link: " + savedLink.getPublicToken());

        return toPublishResponse(savedLink);
    }

    @Transactional
    public SurveyLinkResponse deactivateLink(Long linkId, String username) {
        SurveyLink link = linkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey link not found with ID: " + linkId));

        link.setActive(false);
        link.setRevokedAt(OffsetDateTime.now());
        SurveyLink updated = linkRepository.save(link);

        auditLogService.logAction(username, "LINK_DEACTIVATED", "SURVEY_LINK", linkId.toString(), "Deactivated survey link");
        return templateService.toLinkResponse(updated);
    }

    @Transactional
    public PublishResponse regenerateLink(Long templateId, PublishRequest request, String username) {
        PublishRequest req = request != null ? request : new PublishRequest();
        req.setForceNewLink(true);
        return publishSurvey(templateId, req, username);
    }

    @Transactional(readOnly = true)
    public List<SurveyLinkResponse> getLinksForSurvey(Long templateId) {
        return linkRepository.findBySurveyVersion_Template_IdOrderByCreatedAtDesc(templateId).stream()
                .map(templateService::toLinkResponse)
                .collect(Collectors.toList());
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return UUID.randomUUID().toString().replace("-", "") + HexFormat.of().formatHex(randomBytes).substring(0, 16);
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private PublishResponse toPublishResponse(SurveyLink link) {
        PublishResponse res = new PublishResponse();
        res.setTemplateId(link.getSurveyVersion().getTemplate().getId());
        res.setLinkId(link.getId());
        res.setPublicToken(link.getPublicToken());
        res.setSurveyUrl(publicSurveyBaseUrl + "/" + link.getPublicToken());
        res.setStatus(link.getSurveyVersion().getTemplate().getStatus().name());
        res.setActive(link.getActive());
        res.setVersionNumber(link.getSurveyVersion().getVersionNumber());
        res.setExpiresAt(link.getExpiresAt());
        res.setMaxResponses(link.getMaxResponses());
        return res;
    }
}
