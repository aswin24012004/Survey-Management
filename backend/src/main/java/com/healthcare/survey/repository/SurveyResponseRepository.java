package com.healthcare.survey.repository;

import com.healthcare.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    Optional<SurveyResponse> findByReferenceId(String referenceId);
    List<SurveyResponse> findBySurveyLink_SurveyVersion_Template_IdOrderBySubmittedAtDesc(Long templateId);
    List<SurveyResponse> findTop10ByOrderBySubmittedAtDesc();
    long countBySurveyLink_SurveyVersion_Template_Id(Long templateId);

    @Query("SELECT AVG(r.overallRating) FROM SurveyResponse r WHERE r.surveyLink.surveyVersion.template.id = :templateId AND r.overallRating IS NOT NULL")
    Double calculateAverageRatingForTemplate(@Param("templateId") Long templateId);

    @Query("SELECT AVG(r.overallRating) FROM SurveyResponse r WHERE r.overallRating IS NOT NULL")
    Double calculateOverallAverageRating();
}
