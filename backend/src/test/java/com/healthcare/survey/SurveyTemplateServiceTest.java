package com.healthcare.survey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.QuestionDefinitionDto;
import com.healthcare.survey.dto.SurveyTemplateRequest;
import com.healthcare.survey.dto.SurveyTemplateResponse;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.exception.InvalidSurveyException;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.SurveyLinkRepository;
import com.healthcare.survey.repository.SurveyResponseRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import com.healthcare.survey.repository.SurveyVersionRepository;
import com.healthcare.survey.service.AuditLogService;
import com.healthcare.survey.service.SurveyTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyTemplateServiceTest {

    @Mock
    private SurveyTemplateRepository templateRepository;
    @Mock
    private SurveyLinkRepository linkRepository;
    @Mock
    private SurveyResponseRepository responseRepository;
    @Mock
    private SurveyVersionRepository versionRepository;
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;

    private ObjectMapper objectMapper;
    private SurveyTemplateService templateService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        templateService = new SurveyTemplateService(
                templateRepository,
                linkRepository,
                responseRepository,
                versionRepository,
                userRepository,
                auditLogService,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should successfully create a valid survey template")
    void testCreateValidSurveyTemplate() {
        AppUser admin = new AppUser("admin", "admin@hospital.com", "hash", "ROLE_ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        List<QuestionDefinitionDto> questions = new ArrayList<>();
        QuestionDefinitionDto q1 = new QuestionDefinitionDto("q1", "Rate waiting time", QuestionType.RATING, true);
        q1.setMin(1);
        q1.setMax(5);
        questions.add(q1);

        SurveyTemplateRequest request = new SurveyTemplateRequest("Patient Survey", "Feedback", questions);

        when(templateRepository.save(any(SurveyTemplate.class))).thenAnswer(invocation -> {
            SurveyTemplate t = invocation.getArgument(0);
            t.setId(10L);
            t.setReferenceId("tmpl-12345");
            return t;
        });

        SurveyTemplateResponse response = templateService.createTemplate(request, "admin");

        assertNotNull(response);
        assertEquals("Patient Survey", response.getName());
        assertEquals(SurveyStatus.DRAFT, response.getStatus());
        verify(templateRepository, times(1)).save(any(SurveyTemplate.class));
        verify(auditLogService, times(1)).logAction(eq("admin"), eq("SURVEY_CREATED"), eq("SURVEY_TEMPLATE"), any(), any());
    }

    @Test
    @DisplayName("Should reject survey template with blank name")
    void testRejectBlankName() {
        SurveyTemplateRequest request = new SurveyTemplateRequest("", "Desc", List.of(
                new QuestionDefinitionDto("q1", "Text", QuestionType.SHORT_TEXT, false)
        ));

        assertThrows(InvalidSurveyException.class, () -> templateService.createTemplate(request, "admin"));
    }

    @Test
    @DisplayName("Should reject survey template with no questions")
    void testRejectEmptyQuestions() {
        SurveyTemplateRequest request = new SurveyTemplateRequest("Survey", "Desc", List.of());

        assertThrows(InvalidSurveyException.class, () -> templateService.createTemplate(request, "admin"));
    }

    @Test
    @DisplayName("Should reject invalid rating configuration where min >= max")
    void testRejectInvalidRatingMinMax() {
        QuestionDefinitionDto q = new QuestionDefinitionDto("q1", "Rating", QuestionType.RATING, true);
        q.setMin(5);
        q.setMax(3);

        SurveyTemplateRequest request = new SurveyTemplateRequest("Survey", "Desc", List.of(q));

        assertThrows(InvalidSurveyException.class, () -> templateService.createTemplate(request, "admin"));
    }

    @Test
    @DisplayName("Should reject choice questions with fewer than two options")
    void testRejectChoiceWithFewerThanTwoOptions() {
        QuestionDefinitionDto q = new QuestionDefinitionDto("q1", "Select department", QuestionType.SINGLE_CHOICE, true);
        q.setOptions(List.of("Only One Option"));

        SurveyTemplateRequest request = new SurveyTemplateRequest("Survey", "Desc", List.of(q));

        assertThrows(InvalidSurveyException.class, () -> templateService.createTemplate(request, "admin"));
    }

    @Test
    @DisplayName("Should reject duplicate question IDs")
    void testRejectDuplicateQuestionKeys() {
        QuestionDefinitionDto q1 = new QuestionDefinitionDto("q1", "Question 1", QuestionType.SHORT_TEXT, false);
        QuestionDefinitionDto q2 = new QuestionDefinitionDto("q1", "Question 2", QuestionType.SHORT_TEXT, false);

        SurveyTemplateRequest request = new SurveyTemplateRequest("Survey", "Desc", List.of(q1, q2));

        assertThrows(InvalidSurveyException.class, () -> templateService.createTemplate(request, "admin"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when survey ID does not exist")
    void testGetSurveyNotFound() {
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> templateService.getTemplateById(999L));
    }
}
