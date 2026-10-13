package com.healthcare.survey.repository;

import com.healthcare.survey.entity.SurveyLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyLinkRepository extends JpaRepository<SurveyLink, Long> {
    Optional<SurveyLink> findByPublicToken(String publicToken);
    Optional<SurveyLink> findByPublicTokenHash(String publicTokenHash);
    Optional<SurveyLink> findTopBySurveyVersion_Template_IdAndActiveTrueOrderByCreatedAtDesc(Long templateId);
    List<SurveyLink> findBySurveyVersion_Template_IdOrderByCreatedAtDesc(Long templateId);
    long countByActiveTrue();
}
