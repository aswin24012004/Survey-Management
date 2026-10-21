package com.healthcare.survey.config;

import com.healthcare.survey.entity.*;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository userRepository;
    private final SurveyTemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.initial-password:Admin@123456}")
    private String initialPassword;

    public DataInitializer(AppUserRepository userRepository,
                           SurveyTemplateRepository templateRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDefaultData() {
        AppUser adminUser = initAdminUser();
        initDefaultSurveyTemplate(adminUser);
    }

    private AppUser initAdminUser() {
        return userRepository.findByUsername("admin").map(existing -> {
            boolean seededHash = "$2a$10$P2NrkJqT9g1bZc00j9V31eQvHjXQ7Z03t3jE3wN1XoE0jN8m4QkXe".equals(existing.getPasswordHash());
            boolean defaultPassword = passwordEncoder.matches("Admin@123456", existing.getPasswordHash());
            if (seededHash || (defaultPassword && !"Admin@123456".equals(initialPassword))) {
                log.info("Replacing seeded administrator password");
                existing.setPasswordHash(passwordEncoder.encode(initialPassword));
                return userRepository.save(existing);
            }
            return existing;
        }).orElseGet(() -> {
            log.info("Creating default administrator account: admin / admin@qms.healthcare");
            AppUser admin = new AppUser(
                    "admin",
                    "admin@qms.healthcare",
                    passwordEncoder.encode(initialPassword),
                    "ROLE_ADMIN"
            );
            return userRepository.save(admin);
        });
    }

    private void initDefaultSurveyTemplate(AppUser adminUser) {
        if (templateRepository.count() == 0) {
            log.info("Seeding initial Hospital Patient Satisfaction Survey template");
            SurveyTemplate template = new SurveyTemplate();
            template.setReferenceId("tmpl-patient-satisfaction-001");
            template.setName("Hospital Patient Satisfaction Survey");
            template.setDescription("Patient experience feedback to improve hospital outpatient and inpatient care quality.");
            template.setCategory("PATIENT_CARE");
            template.setStatus(SurveyStatus.DRAFT);
            template.setCreatedBy(adminUser);

            SurveyQuestion q1 = new SurveyQuestion();
            q1.setQuestionKey("q1");
            q1.setQuestionText("How satisfied are you with the waiting time before your consultation?");
            q1.setQuestionType(QuestionType.RATING);
            q1.setRequired(true);
            q1.setDisplayOrder(1);
            q1.setConfigurationJson("{\"min\":1,\"max\":5,\"placeholder\":\"Select rating 1 to 5\",\"helpText\":\"1 = Very Dissatisfied, 5 = Very Satisfied\"}");
            template.addQuestion(q1);

            SurveyQuestion q2 = new SurveyQuestion();
            q2.setQuestionKey("q2");
            q2.setQuestionText("Did the doctor explain your medical condition and treatment clearly?");
            q2.setQuestionType(QuestionType.SINGLE_CHOICE);
            q2.setRequired(true);
            q2.setDisplayOrder(2);
            q2.setConfigurationJson("{\"options\":[\"Yes\",\"No\",\"Partially\"]}");
            template.addQuestion(q2);

            SurveyQuestion q3 = new SurveyQuestion();
            q3.setQuestionKey("q3");
            q3.setQuestionText("How would you rate the cleanliness of the hospital and patient rooms?");
            q3.setQuestionType(QuestionType.RATING);
            q3.setRequired(true);
            q3.setDisplayOrder(3);
            q3.setConfigurationJson("{\"min\":1,\"max\":5,\"placeholder\":\"Select rating 1 to 5\"}");
            template.addQuestion(q3);

            SurveyQuestion q4 = new SurveyQuestion();
            q4.setQuestionKey("q4");
            q4.setQuestionText("Would you recommend our healthcare facility to family and friends?");
            q4.setQuestionType(QuestionType.YES_NO);
            q4.setRequired(true);
            q4.setDisplayOrder(4);
            q4.setConfigurationJson("{}");
            template.addQuestion(q4);

            SurveyQuestion q5 = new SurveyQuestion();
            q5.setQuestionKey("q5");
            q5.setQuestionText("Please share any suggestions or compliments for our clinical and nursing staff.");
            q5.setQuestionType(QuestionType.LONG_TEXT);
            q5.setRequired(false);
            q5.setDisplayOrder(5);
            q5.setConfigurationJson("{\"placeholder\":\"Write your feedback here...\"}");
            template.addQuestion(q5);

            templateRepository.save(template);
        }
    }
}
