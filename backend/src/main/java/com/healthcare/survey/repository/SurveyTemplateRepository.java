package com.healthcare.survey.repository;

import com.healthcare.survey.entity.SurveyStatus;
import com.healthcare.survey.entity.SurveyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate, Long> {
    Optional<SurveyTemplate> findByReferenceId(String referenceId);
    List<SurveyTemplate> findAllByOrderByCreatedAtDesc();
    long countByStatus(SurveyStatus status);
}
