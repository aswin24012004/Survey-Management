package com.healthcare.survey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.*;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.repository.SurveyLinkRepository;
import com.healthcare.survey.repository.SurveyResponseAnswerRepository;
import com.healthcare.survey.repository.SurveyResponseRepository;
import com.healthcare.survey.repository.SurveyTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SurveyAnalyticsService {

    private final SurveyTemplateRepository templateRepository;
    private final SurveyLinkRepository linkRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyResponseAnswerRepository answerRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public SurveyAnalyticsService(SurveyTemplateRepository templateRepository,
                                  SurveyLinkRepository linkRepository,
                                  SurveyResponseRepository responseRepository,
                                  SurveyResponseAnswerRepository answerRepository,
                                  AuditLogService auditLogService,
                                  ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.linkRepository = linkRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalSurveys(templateRepository.count());
        stats.setPublishedSurveys(templateRepository.countByStatus(SurveyStatus.PUBLISHED));
        stats.setDraftSurveys(templateRepository.countByStatus(SurveyStatus.DRAFT));
        stats.setActiveLinks(linkRepository.countByActiveTrue());
        stats.setTotalResponses(responseRepository.count());

        Double avg = responseRepository.calculateOverallAverageRating();
        stats.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);

        List<RecentSubmissionDto> recent = responseRepository.findTop10ByOrderBySubmittedAtDesc().stream().map(r -> {
            String surveyName = r.getSurveyLink() != null && r.getSurveyLink().getSurveyVersion() != null
                    ? r.getSurveyLink().getSurveyVersion().getTemplate().getName()
                    : "Unknown Survey";
            return new RecentSubmissionDto(
                    r.getReferenceId(),
                    surveyName,
                    r.getRespondentName() != null ? r.getRespondentName() : "Anonymous",
                    r.getOverallRating(),
                    r.getSubmittedAt()
            );
        }).collect(Collectors.toList());

        stats.setRecentSubmissions(recent);
        return stats;
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseDto> getSurveyResponses(Long templateId) {
        templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with ID: " + templateId));

        List<SurveyResponse> responses = responseRepository.findBySurveyLink_SurveyVersion_Template_IdOrderBySubmittedAtDesc(templateId);

        return responses.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurveyAnalyticsDto getSurveyAnalytics(Long templateId) {
        SurveyTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with ID: " + templateId));

        List<SurveyResponse> responses = responseRepository.findBySurveyLink_SurveyVersion_Template_IdOrderBySubmittedAtDesc(templateId);
        List<SurveyQuestion> questions = template.getQuestions();

        SurveyAnalyticsDto analytics = new SurveyAnalyticsDto();
        analytics.setSurveyId(template.getId());
        analytics.setSurveyName(template.getName());
        analytics.setTotalResponses(responses.size());

        Double avg = responseRepository.calculateAverageRatingForTemplate(templateId);
        analytics.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);

        // Overall rating distribution (1 to 5)
        Map<Integer, Long> ratingDist = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDist.put(i, 0L);
        }
        // Submissions timeline (by day YYYY-MM-DD)
        Map<String, Long> timeline = new TreeMap<>();

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (SurveyResponse r : responses) {
            if (r.getOverallRating() != null && r.getOverallRating() >= 1 && r.getOverallRating() <= 5) {
                ratingDist.put(r.getOverallRating(), ratingDist.get(r.getOverallRating()) + 1);
            }
            if (r.getSubmittedAt() != null) {
                String day = r.getSubmittedAt().format(dateFmt);
                timeline.put(day, timeline.getOrDefault(day, 0L) + 1);
            }
        }
        analytics.setRatingDistribution(ratingDist);
        analytics.setTimeline(timeline);

        // Calculate Question-wise Stats
        List<QuestionStatDto> questionStats = new ArrayList<>();
        for (SurveyQuestion q : questions) {
            QuestionStatDto qStat = new QuestionStatDto(q.getQuestionKey(), q.getQuestionText(), q.getQuestionType());
            long totalAnswers = 0;
            double ratingSum = 0;
            long ratingCount = 0;
            Map<String, Long> choiceDist = new HashMap<>();

            for (SurveyResponse r : responses) {
                for (SurveyResponseAnswer a : r.getAnswers()) {
                    if (a.getQuestionKey().equalsIgnoreCase(q.getQuestionKey())) {
                        totalAnswers++;
                        try {
                            Object parsed = objectMapper.readValue(a.getAnswerJson(), Object.class);
                            if (q.getQuestionType() == QuestionType.RATING && parsed instanceof Number num) {
                                ratingSum += num.doubleValue();
                                ratingCount++;
                            } else if (parsed instanceof List<?> list) {
                                for (Object item : list) {
                                    String key = String.valueOf(item);
                                    choiceDist.put(key, choiceDist.getOrDefault(key, 0L) + 1);
                                }
                            } else {
                                String key = String.valueOf(parsed);
                                choiceDist.put(key, choiceDist.getOrDefault(key, 0L) + 1);
                            }
                        } catch (Exception ignored) {
                            String raw = a.getAnswerJson().replace("\"", "");
                            choiceDist.put(raw, choiceDist.getOrDefault(raw, 0L) + 1);
                        }
                    }
                }
            }

            qStat.setTotalAnswers(totalAnswers);
            if (ratingCount > 0) {
                qStat.setAverageRating(Math.round((ratingSum / ratingCount) * 10.0) / 10.0);
            }
            qStat.setChoiceDistribution(choiceDist);
            questionStats.add(qStat);
        }

        analytics.setQuestionStats(questionStats);
        return analytics;
    }

    @Transactional(readOnly = true)
    public byte[] exportResponsesToCsv(Long templateId, String username) {
        SurveyTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with ID: " + templateId));

        List<SurveyResponse> responses = responseRepository.findBySurveyLink_SurveyVersion_Template_IdOrderBySubmittedAtDesc(templateId);
        List<SurveyQuestion> questions = template.getQuestions();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            // Write BOM for Excel UTF-8 compatibility
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // Headers
            List<String> headers = new ArrayList<>(List.of("Reference ID", "Submitted At", "Respondent Name", "Mobile Number", "Email", "Overall Rating", "Overall Comments"));
            for (SurveyQuestion q : questions) {
                headers.add(escapeCsv(q.getQuestionText() + " (" + q.getQuestionKey() + ")"));
            }
            writer.println(String.join(",", headers));

            DateTimeFormatter isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (SurveyResponse r : responses) {
                List<String> row = new ArrayList<>();
                row.add(escapeCsv(r.getReferenceId()));
                row.add(escapeCsv(r.getSubmittedAt() != null ? r.getSubmittedAt().format(isoFmt) : ""));
                row.add(escapeCsv(r.getRespondentName() != null ? r.getRespondentName() : ""));
                row.add(escapeCsv(r.getMobileNumber() != null ? r.getMobileNumber() : ""));
                row.add(escapeCsv(r.getEmailAddress() != null ? r.getEmailAddress() : ""));
                row.add(escapeCsv(r.getOverallRating() != null ? r.getOverallRating().toString() : ""));
                row.add(escapeCsv(r.getOverallComments() != null ? r.getOverallComments() : ""));

                Map<String, String> answerMap = new HashMap<>();
                for (SurveyResponseAnswer a : r.getAnswers()) {
                    answerMap.put(a.getQuestionKey(), a.getAnswerJson());
                }

                for (SurveyQuestion q : questions) {
                    String ansRaw = answerMap.get(q.getQuestionKey());
                    if (ansRaw == null) {
                        row.add("");
                    } else {
                        try {
                            Object obj = objectMapper.readValue(ansRaw, Object.class);
                            if (obj instanceof List<?> l) {
                                row.add(escapeCsv(String.join("; ", l.stream().map(Object::toString).toList())));
                            } else {
                                row.add(escapeCsv(String.valueOf(obj)));
                            }
                        } catch (Exception e) {
                            row.add(escapeCsv(ansRaw));
                        }
                    }
                }
                writer.println(String.join(",", row));
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }

        auditLogService.logAction(username, "RESPONSES_EXPORTED", "SURVEY_TEMPLATE", templateId.toString(),
                "Exported " + responses.size() + " responses for survey: " + template.getName());

        return baos.toByteArray();
    }

    private SurveyResponseDto toResponseDto(SurveyResponse response) {
        SurveyResponseDto dto = new SurveyResponseDto();
        dto.setId(response.getId());
        dto.setReferenceId(response.getReferenceId());
        dto.setSurveyLinkId(response.getSurveyLink() != null ? response.getSurveyLink().getId() : null);
        dto.setRespondentName(response.getRespondentName());
        dto.setMobileNumber(response.getMobileNumber());
        dto.setEmailAddress(response.getEmailAddress());
        dto.setOverallRating(response.getOverallRating());
        dto.setOverallComments(response.getOverallComments());
        dto.setStatus(response.getStatus());
        dto.setSubmittedAt(response.getSubmittedAt());

        Map<String, Object> answers = new HashMap<>();
        if (response.getAnswers() != null) {
            for (SurveyResponseAnswer a : response.getAnswers()) {
                try {
                    Object val = objectMapper.readValue(a.getAnswerJson(), Object.class);
                    answers.put(a.getQuestionKey(), val);
                } catch (Exception e) {
                    answers.put(a.getQuestionKey(), a.getAnswerJson());
                }
            }
        }
        dto.setAnswers(answers);
        return dto;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.replace("\r", " ").replace("\n", " ");
        if (clean.contains(",") || clean.contains("\"") || clean.contains(";")) {
            return "\"" + clean.replace("\"", "\"\"") + "\"";
        }
        return clean;
    }
}
