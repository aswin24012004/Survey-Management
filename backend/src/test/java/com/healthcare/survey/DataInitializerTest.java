package com.healthcare.survey;

import com.healthcare.survey.config.DataInitializer;
import com.healthcare.survey.entity.AppUser;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataInitializerTest {
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final SurveyTemplateRepository templates = mock(SurveyTemplateRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private DataInitializer initializer() {
        DataInitializer initializer = new DataInitializer(users, templates, encoder);
        ReflectionTestUtils.setField(initializer, "initialPassword", "generated-hosting-password");
        when(templates.count()).thenReturn(1L);
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return initializer;
    }

    @Test
    void preservesChangedAdminPasswordAcrossRestarts() {
        AppUser admin = new AppUser("admin", "admin@example.com", encoder.encode("changed-password"), "ROLE_ADMIN");
        String originalHash = admin.getPasswordHash();
        when(users.findByUsername("admin")).thenReturn(Optional.of(admin));
        initializer().initDefaultData();
        assertEquals(originalHash, admin.getPasswordHash());
        verify(users, never()).save(any());
    }

    @Test
    void replacesKnownDevelopmentPasswordWithHostingSecret() {
        AppUser admin = new AppUser("admin", "admin@example.com", encoder.encode("Admin@123456"), "ROLE_ADMIN");
        when(users.findByUsername("admin")).thenReturn(Optional.of(admin));
        initializer().initDefaultData();
        assertTrue(encoder.matches("generated-hosting-password", admin.getPasswordHash()));
    }

    @Test
    void replacesMigrationSeedHashWithHostingSecret() {
        AppUser admin = new AppUser("admin", "admin@example.com",
                "$2a$10$P2NrkJqT9g1bZc00j9V31eQvHjXQ7Z03t3jE3wN1XoE0jN8m4QkXe", "ROLE_ADMIN");
        when(users.findByUsername("admin")).thenReturn(Optional.of(admin));
        initializer().initDefaultData();
        assertTrue(encoder.matches("generated-hosting-password", admin.getPasswordHash()));
    }
}
