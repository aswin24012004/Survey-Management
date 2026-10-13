package com.healthcare.survey.repository;

import com.healthcare.survey.entity.SurveyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyVersionRepository extends JpaRepository<SurveyVersion, Long> {
    Optional<SurveyVersion> findTopByTemplateIdOrderByVersionNumberDesc(Long templateId);
    List<SurveyVersion> findByTemplateIdOrderByVersionNumberDesc(Long templateId);
}
