package com.healthcare.survey.service;

import com.healthcare.survey.dto.AuditLogDto;
import com.healthcare.survey.entity.AppUser;
import com.healthcare.survey.entity.AuditLog;
import com.healthcare.survey.repository.AppUserRepository;
import com.healthcare.survey.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, AppUserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String username, String action, String entityType, String entityId, String details) {
        try {
            AppUser actor = null;
            if (username != null && !username.isBlank()) {
                actor = userRepository.findByUsername(username).orElse(null);
            }
            AuditLog auditLog = new AuditLog(actor, action, entityType, entityId, details);
            auditLogRepository.save(auditLog);
            log.info("AUDIT: User [{}] performed [{}] on [{}:{}]", username, action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc().stream().map(log -> {
            AuditLogDto dto = new AuditLogDto();
            dto.setId(log.getId());
            dto.setActorUsername(log.getActorUser() != null ? log.getActorUser().getUsername() : "SYSTEM");
            dto.setAction(log.getAction());
            dto.setEntityType(log.getEntityType());
            dto.setEntityId(log.getEntityId());
            dto.setDetails(log.getDetails());
            dto.setCreatedAt(log.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
}
