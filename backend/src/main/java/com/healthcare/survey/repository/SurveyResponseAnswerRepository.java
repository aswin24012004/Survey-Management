package com.healthcare.survey.repository;

import com.healthcare.survey.entity.SurveyResponseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResponseAnswerRepository extends JpaRepository<SurveyResponseAnswer, Long> {
    List<SurveyResponseAnswer> findByResponseId(Long responseId);
    List<SurveyResponseAnswer> findByResponse_SurveyLink_SurveyVersion_Template_Id(Long templateId);
}
