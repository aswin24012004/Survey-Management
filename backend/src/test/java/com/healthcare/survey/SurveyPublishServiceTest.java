package com.healthcare.survey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.PublishRequest;
import com.healthcare.survey.dto.PublishResponse;
import com.healthcare.survey.dto.SurveyLinkResponse;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.exception.InvalidSurveyException;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.SurveyLinkRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import com.healthcare.survey.repository.SurveyVersionRepository;
import com.healthcare.survey.service.AuditLogService;
import com.healthcare.survey.service.SurveyPublishService;
import com.healthcare.survey.service.SurveyTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyPublishServiceTest {

    @Mock
    private SurveyTemplateRepository templateRepository;
    @Mock
    private SurveyVersionRepository versionRepository;
    @Mock
    private SurveyLinkRepository linkRepository;
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private SurveyTemplateService templateService;
    @Mock
    private AuditLogService auditLogService;

    private ObjectMapper objectMapper;
    private SurveyPublishService publishService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        publishService = new SurveyPublishService(
                templateRepository,
                versionRepository,
                linkRepository,
                userRepository,
                templateService,
                auditLogService,
                objectMapper
        );
        ReflectionTestUtils.setField(publishService, "publicSurveyBaseUrl", "http://localhost:5173/survey");
    }

    @Test
    @DisplayName("Should successfully publish a survey template with questions")
    void testSuccessfulPublish() {
        SurveyTemplate template = new SurveyTemplate();
        template.setId(1L);
        template.setName("Patient Satisfaction");
        template.setStatus(SurveyStatus.DRAFT);

        SurveyQuestion q1 = new SurveyQuestion();
        q1.setQuestionKey("q1");
        q1.setQuestionText("Cleanliness rating");
        q1.setQuestionType(QuestionType.RATING);
        template.addQuestion(q1);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(linkRepository.findTopBySurveyVersion_Template_IdAndActiveTrueOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(versionRepository.findTopByTemplateIdOrderByVersionNumberDesc(1L)).thenReturn(Optional.empty());

        when(versionRepository.save(any(SurveyVersion.class))).thenAnswer(invocation -> {
            SurveyVersion v = invocation.getArgument(0);
            v.setId(100L);
            return v;
        });

        when(linkRepository.save(any(SurveyLink.class))).thenAnswer(invocation -> {
            SurveyLink l = invocation.getArgument(0);
            l.setId(200L);
            return l;
        });

        PublishResponse response = publishService.publishSurvey(1L, new PublishRequest(), "admin");

        assertNotNull(response);
        assertEquals(1L, response.getTemplateId());
        assertEquals(200L, response.getLinkId());
        assertTrue(response.getActive());
        assertNotNull(response.getSurveyUrl());
        assertTrue(response.getSurveyUrl().contains("http://localhost:5173/survey/"));
        assertEquals(1, response.getVersionNumber());

        verify(templateRepository).save(template);
        assertEquals(SurveyStatus.PUBLISHED, template.getStatus());
    }

    @Test
    @DisplayName("Publishing the same survey twice should return the existing active link")
    void testPublishSameSurveyTwiceReturnsExistingLink() {
        SurveyTemplate template = new SurveyTemplate();
        template.setId(1L);
        template.setName("Patient Satisfaction");
        template.setStatus(SurveyStatus.PUBLISHED);

        SurveyQuestion q1 = new SurveyQuestion();
        q1.setQuestionKey("q1");
        q1.setQuestionText("Cleanliness rating");
        q1.setQuestionType(QuestionType.RATING);
        template.addQuestion(q1);

        SurveyVersion version = new SurveyVersion();
        version.setId(10L);
        version.setTemplate(template);
        version.setVersionNumber(1);

        SurveyLink activeLink = new SurveyLink();
        activeLink.setId(50L);
        activeLink.setSurveyVersion(version);
        activeLink.setPublicToken("token-existing-12345");
        activeLink.setActive(true);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(linkRepository.findTopBySurveyVersion_Template_IdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(activeLink));

        PublishResponse response = publishService.publishSurvey(1L, new PublishRequest(), "admin");

        assertNotNull(response);
        assertEquals(50L, response.getLinkId());
        assertEquals("token-existing-12345", response.getPublicToken());
        // Verify no new version or link was saved
        verify(versionRepository, never()).save(any(SurveyVersion.class));
    }

    @Test
    @DisplayName("Should reject publishing survey template with no questions")
    void testRejectPublishEmptyQuestions() {
        SurveyTemplate template = new SurveyTemplate();
        template.setId(2L);
        template.setName("Empty Template");

        when(templateRepository.findById(2L)).thenReturn(Optional.of(template));

        assertThrows(InvalidSurveyException.class, () -> publishService.publishSurvey(2L, new PublishRequest(), "admin"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if template does not exist")
    void testPublishNonExistentSurvey() {
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publishService.publishSurvey(999L, new PublishRequest(), "admin"));
    }

    @Test
    @DisplayName("Should successfully deactivate an active survey link")
    void testDeactivateLink() {
        SurveyLink link = new SurveyLink();
        link.setId(10L);
        link.setActive(true);

        when(linkRepository.findById(10L)).thenReturn(Optional.of(link));
        when(linkRepository.save(any(SurveyLink.class))).thenAnswer(i -> i.getArgument(0));

        SurveyLinkResponse dummyResponse = new SurveyLinkResponse();
        dummyResponse.setId(10L);
        dummyResponse.setActive(false);
        when(templateService.toLinkResponse(any(SurveyLink.class))).thenReturn(dummyResponse);

        SurveyLinkResponse result = publishService.deactivateLink(10L, "admin");

        assertNotNull(result);
        assertFalse(result.getActive());
        assertFalse(link.getActive());
        assertNotNull(link.getRevokedAt());
    }
}
