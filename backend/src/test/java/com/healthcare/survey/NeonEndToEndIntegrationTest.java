package com.healthcare.survey;

import com.healthcare.survey.dto.*;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import com.healthcare.survey.service.AuthService;
import com.healthcare.survey.service.PublicSurveyService;
import com.healthcare.survey.service.SurveyPublishService;
import com.healthcare.survey.service.SurveyTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("neon-integration")
public class NeonEndToEndIntegrationTest {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private SurveyTemplateRepository templateRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private SurveyTemplateService templateService;

    @Autowired
    private SurveyPublishService publishService;

    @Autowired
    private PublicSurveyService publicSurveyService;

    @Test
    @DisplayName("Verify Neon Postgres end-to-end: Auth, Templates, Publishing, and Responses")
    void testNeonEndToEndWorkflow() {
        // 1. Verify default admin user seeded in Neon Postgres
        AppUser admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new AssertionError("Admin user was not found in Neon database"));
        assertEquals("admin@qms.healthcare", admin.getEmail());

        // 2. Verify Authentication works with Neon
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        AuthResponse auth = authService.login(new LoginRequest("admin", "Admin@123456"), httpResponse);
        assertNotNull(auth.getAccessToken());
        assertEquals("admin", auth.getUser().getUsername());

        // 3. Create a new survey template on Neon
        QuestionDefinitionDto q1 = new QuestionDefinitionDto("q1", "How was the hospital reception service?", QuestionType.RATING, true);
        q1.setMin(1);
        q1.setMax(5);
        QuestionDefinitionDto q2 = new QuestionDefinitionDto("q2", "Would you visit again?", QuestionType.YES_NO, true);

        SurveyTemplateRequest templateReq = new SurveyTemplateRequest(
                "Neon Integration Clinic Survey",
                "Testing real-time dynamic survey persistence in Neon Postgres",
                List.of(q1, q2)
        );
        templateReq.setCategory("CLINICAL");

        SurveyTemplateResponse created = templateService.createTemplate(templateReq, "admin");
        assertNotNull(created.getId());
        assertEquals("Neon Integration Clinic Survey", created.getName());

        // 4. Publish survey template and generate public token
        PublishRequest pubReq = new PublishRequest();
        PublishResponse published = publishService.publishSurvey(created.getId(), pubReq, "admin");
        assertNotNull(published.getPublicToken());
        assertTrue(published.getActive());

        // 5. Public respondent fetches survey from Neon
        PublicSurveyDto publicSurvey = publicSurveyService.getPublicSurvey(published.getPublicToken());
        assertEquals("Neon Integration Clinic Survey", publicSurvey.getName());
        assertEquals(2, publicSurvey.getQuestions().size());

        // 6. Public respondent submits response to Neon
        SurveySubmissionRequest submissionReq = new SurveySubmissionRequest(
                new RespondentDetailsDto("Neon Test Patient", "9876543210", "patient@neon-test.com", 5, "Outstanding care!"),
                List.of(
                        new SubmissionAnswerDto("q1", 5),
                        new SubmissionAnswerDto("q2", "Yes")
                )
        );
        SubmissionResponseDto submissionRes = publicSurveyService.submitSurveyResponse(published.getPublicToken(), submissionReq);
        assertNotNull(submissionRes.getConfirmationId());
        assertNotNull(submissionRes.getMessage());

        System.out.println("===============================================================================");
        System.out.println(">>> SUCCESS: Full End-to-End workflow verified on Neon PostgreSQL!");
        System.out.println(">>> Database Host: Neon Lakebase Postgres (aws us-east-2)");
        System.out.println(">>> Created Survey ID: " + created.getId());
        System.out.println(">>> Published Token: " + published.getPublicToken());
        System.out.println(">>> Submitted Confirmation ID: " + submissionRes.getConfirmationId());
        System.out.println("===============================================================================");
    }
}
