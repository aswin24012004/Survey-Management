package com.healthcare.survey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.*;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.exception.InvalidSurveyException;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.exception.SurveyClosedException;
import com.healthcare.survey.repository.SurveyLinkRepository;
import com.healthcare.survey.repository.SurveyResponseAnswerRepository;
import com.healthcare.survey.repository.SurveyResponseRepository;
import com.healthcare.survey.service.PublicSurveyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicSurveyServiceTest {

    @Mock
    private SurveyLinkRepository linkRepository;
    @Mock
    private SurveyResponseRepository responseRepository;
    @Mock
    private SurveyResponseAnswerRepository answerRepository;

    private ObjectMapper objectMapper;
    private PublicSurveyService publicSurveyService;

    private SurveyLink mockLink;
    private SurveyTemplateResponse mockSnapshot;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        publicSurveyService = new PublicSurveyService(
                linkRepository,
                responseRepository,
                answerRepository,
                objectMapper
        );

        // Prepare test snapshot
        mockSnapshot = new SurveyTemplateResponse();
        mockSnapshot.setId(1L);
        mockSnapshot.setReferenceId("tmpl-001");
        mockSnapshot.setName("Patient Experience");
        mockSnapshot.setDescription("Feedback survey");

        List<QuestionDefinitionDto> questions = new ArrayList<>();

        QuestionDefinitionDto q1 = new QuestionDefinitionDto("q1", "Waiting time satisfaction", QuestionType.RATING, true);
        q1.setMin(1);
        q1.setMax(5);
        questions.add(q1);

        QuestionDefinitionDto q2 = new QuestionDefinitionDto("q2", "Doctor explained clearly?", QuestionType.SINGLE_CHOICE, true);
        q2.setOptions(List.of("Yes", "No", "Partially"));
        questions.add(q2);

        QuestionDefinitionDto q3 = new QuestionDefinitionDto("q3", "Comments", QuestionType.LONG_TEXT, false);
        questions.add(q3);

        mockSnapshot.setQuestions(questions);

        SurveyVersion version = new SurveyVersion();
        version.setId(10L);
        version.setVersionNumber(1);
        version.setPublishedJson(objectMapper.writeValueAsString(mockSnapshot));

        mockLink = new SurveyLink();
        mockLink.setId(100L);
        mockLink.setPublicToken("valid-token-123");
        mockLink.setPublicTokenHash("hash-123");
        mockLink.setActive(true);
        mockLink.setSurveyVersion(version);
        mockLink.setResponseCount(0);
    }

    @Test
    @DisplayName("Should retrieve public survey by valid token")
    void testGetPublicSurveyValidToken() {
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        PublicSurveyDto survey = publicSurveyService.getPublicSurvey("valid-token-123");

        assertNotNull(survey);
        assertEquals("valid-token-123", survey.getToken());
        assertEquals("Patient Experience", survey.getName());
        assertEquals(3, survey.getQuestions().size());
        assertEquals("q1", survey.getQuestions().get(0).getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown token")
    void testGetPublicSurveyInvalidToken() {
        when(linkRepository.findByPublicToken("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publicSurveyService.getPublicSurvey("non-existent"));
    }

    @Test
    @DisplayName("Should throw SurveyClosedException if link is deactivated")
    void testGetPublicSurveyDeactivated() {
        mockLink.setActive(false);
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        assertThrows(SurveyClosedException.class, () -> publicSurveyService.getPublicSurvey("valid-token-123"));
    }

    @Test
    @DisplayName("Should throw SurveyClosedException if link is expired")
    void testGetPublicSurveyExpired() {
        mockLink.setExpiresAt(OffsetDateTime.now().minusDays(1));
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        assertThrows(SurveyClosedException.class, () -> publicSurveyService.getPublicSurvey("valid-token-123"));
    }

    @Test
    @DisplayName("Should throw SurveyClosedException if max responses limit is reached")
    void testGetPublicSurveyLimitReached() {
        mockLink.setMaxResponses(50);
        mockLink.setResponseCount(50);
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        assertThrows(SurveyClosedException.class, () -> publicSurveyService.getPublicSurvey("valid-token-123"));
    }

    @Test
    @DisplayName("Should submit response successfully with valid answers")
    void testSubmitValidResponse() {
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));
        when(responseRepository.save(any(SurveyResponse.class))).thenAnswer(i -> {
            SurveyResponse r = i.getArgument(0);
            r.setId(500L);
            r.setReferenceId("RESP-ABC123");
            r.setSubmittedAt(OffsetDateTime.now());
            return r;
        });

        RespondentDetailsDto respondent = new RespondentDetailsDto("Arun Kumar", "9876543210", "arun@example.com", 5, "Great hospital");
        List<SubmissionAnswerDto> answers = List.of(
                new SubmissionAnswerDto("q1", 4),
                new SubmissionAnswerDto("q2", "Yes"),
                new SubmissionAnswerDto("q3", "Staff was very kind")
        );

        SurveySubmissionRequest request = new SurveySubmissionRequest(respondent, answers);

        SubmissionResponseDto response = publicSurveyService.submitSurveyResponse("valid-token-123", request);

        assertNotNull(response);
        assertEquals("RESP-ABC123", response.getConfirmationId());
        assertTrue(response.getMessage().contains("successfully submitted"));
        assertEquals(1, mockLink.getResponseCount());
        verify(responseRepository, times(1)).save(any(SurveyResponse.class));
        verify(answerRepository, times(3)).save(any(SurveyResponseAnswer.class));
    }

    @Test
    @DisplayName("Should reject submission when required question answer is missing")
    void testRejectMissingRequiredAnswer() {
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        RespondentDetailsDto respondent = new RespondentDetailsDto("Arun", null, null, null, null);
        // q2 is required but omitted
        List<SubmissionAnswerDto> answers = List.of(
                new SubmissionAnswerDto("q1", 4)
        );

        SurveySubmissionRequest request = new SurveySubmissionRequest(respondent, answers);

        assertThrows(InvalidSurveyException.class, () -> publicSurveyService.submitSurveyResponse("valid-token-123", request));
    }

    @Test
    @DisplayName("Should reject submission with invalid rating value out of bounds")
    void testRejectInvalidRatingValue() {
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        List<SubmissionAnswerDto> answers = List.of(
                new SubmissionAnswerDto("q1", 10), // Max is 5
                new SubmissionAnswerDto("q2", "Yes")
        );

        SurveySubmissionRequest request = new SurveySubmissionRequest(null, answers);

        assertThrows(InvalidSurveyException.class, () -> publicSurveyService.submitSurveyResponse("valid-token-123", request));
    }

    @Test
    @DisplayName("Should reject submission with choice not in published options")
    void testRejectInvalidChoiceOption() {
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        List<SubmissionAnswerDto> answers = List.of(
                new SubmissionAnswerDto("q1", 5),
                new SubmissionAnswerDto("q2", "Unknown Choice") // Allowed: Yes, No, Partially
        );

        SurveySubmissionRequest request = new SurveySubmissionRequest(null, answers);

        assertThrows(InvalidSurveyException.class, () -> publicSurveyService.submitSurveyResponse("valid-token-123", request));
    }

    @Test
    @DisplayName("Should reject submission with unknown question ID")
    void testRejectUnknownQuestionId() {
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));

        List<SubmissionAnswerDto> answers = List.of(
                new SubmissionAnswerDto("q999", "Some answer")
        );

        SurveySubmissionRequest request = new SurveySubmissionRequest(null, answers);

        assertThrows(InvalidSurveyException.class, () -> publicSurveyService.submitSurveyResponse("valid-token-123", request));
    }

    @Test
    @DisplayName("Should support multiple independent submissions on the same public link")
    void testMultipleIndependentSubmissions() {
        when(linkRepository.findByPublicToken("valid-token-123")).thenReturn(Optional.of(mockLink));
        when(responseRepository.save(any(SurveyResponse.class))).thenAnswer(i -> {
            SurveyResponse r = i.getArgument(0);
            r.setId((long) (Math.random() * 1000));
            r.setReferenceId("RESP-" + UUID.randomUUID().toString().substring(0, 6));
            r.setSubmittedAt(OffsetDateTime.now());
            return r;
        });

        // Submission 1
        SurveySubmissionRequest req1 = new SurveySubmissionRequest(
                new RespondentDetailsDto("Alice", "1111111111", "alice@test.com", 4, null),
                List.of(new SubmissionAnswerDto("q1", 4), new SubmissionAnswerDto("q2", "Yes"))
        );
        SubmissionResponseDto res1 = publicSurveyService.submitSurveyResponse("valid-token-123", req1);

        // Submission 2
        SurveySubmissionRequest req2 = new SurveySubmissionRequest(
                new RespondentDetailsDto("Bob", "2222222222", "bob@test.com", 5, null),
                List.of(new SubmissionAnswerDto("q1", 5), new SubmissionAnswerDto("q2", "No"))
        );
        SubmissionResponseDto res2 = publicSurveyService.submitSurveyResponse("valid-token-123", req2);

        assertNotNull(res1);
        assertNotNull(res2);
        assertNotEquals(res1.getConfirmationId(), res2.getConfirmationId());
        assertEquals(2, mockLink.getResponseCount());
        verify(responseRepository, times(2)).save(any(SurveyResponse.class));
    }
}
