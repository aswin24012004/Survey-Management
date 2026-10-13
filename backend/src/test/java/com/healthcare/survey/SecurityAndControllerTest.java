package com.healthcare.survey;

import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Admin endpoint should reject unauthorized requests without token")
    void testAdminEndpointUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/surveys"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin endpoint should permit authorized admin request with valid JWT")
    void testAdminEndpointAuthorizedWithValidToken() throws Exception {
        String token = jwtTokenProvider.generateAccessToken("admin", "ROLE_ADMIN");

        mockMvc.perform(get("/api/admin/surveys")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public survey endpoint should be accessible without authentication token")
    void testPublicEndpointAccessibleWithoutAuth() throws Exception {
        // Will return 404 for non-existent token, but NOT 401 Unauthorized!
        mockMvc.perform(get("/api/public/surveys/any-random-test-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Login with invalid credentials should return 401 Unauthorized")
    void testLoginWithBadCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"invaliduser\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized());
    }
}
