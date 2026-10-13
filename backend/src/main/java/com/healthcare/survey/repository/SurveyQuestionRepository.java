package com.healthcare.survey.repository;

import com.healthcare.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    List<SurveyQuestion> findByTemplateIdOrderByDisplayOrderAsc(Long templateId);
    void deleteByTemplateId(Long templateId);
}
