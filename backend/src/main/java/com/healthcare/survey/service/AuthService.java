package com.healthcare.survey.service;

import com.healthcare.survey.dto.AuthResponse;
import com.healthcare.survey.dto.LoginRequest;
import com.healthcare.survey.dto.UserSummaryDto;
import com.healthcare.survey.entity.AppUser;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.exception.UnauthorizedException;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AppUserRepository userRepository;
    private final AuditLogService auditLogService;

    @Value("${app.jwt.cookie-name:refreshToken}")
    private String refreshCookieName;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       AppUserRepository userRepository,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUser user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        setRefreshTokenCookie(response, refreshToken, (int) (tokenProvider.getRefreshTokenExpirationMs() / 1000));

        auditLogService.logAction(user.getUsername(), "USER_LOGIN", "APP_USER", user.getId().toString(), "Admin logged in");

        UserSummaryDto userSummary = new UserSummaryDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        return new AuthResponse(accessToken, tokenProvider.getAccessTokenExpirationMs() / 1000, userSummary);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found for token"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("User account is disabled");
        }

        String newAccessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        setRefreshTokenCookie(response, newRefreshToken, (int) (tokenProvider.getRefreshTokenExpirationMs() / 1000));

        UserSummaryDto userSummary = new UserSummaryDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        return new AuthResponse(newAccessToken, tokenProvider.getAccessTokenExpirationMs() / 1000, userSummary);
    }

    public void logout(String username, HttpServletResponse response) {
        setRefreshTokenCookie(response, "", 0);
        if (username != null) {
            auditLogService.logAction(username, "USER_LOGOUT", "APP_USER", username, "Admin logged out");
        }
    }

    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUser(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return new UserSummaryDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(false) // Set to true in HTTPS production
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
