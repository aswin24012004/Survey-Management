package com.healthcare.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.survey.dto.*;
import com.healthcare.survey.entity.*;
import com.healthcare.survey.exception.InvalidSurveyException;
import com.healthcare.survey.exception.ResourceNotFoundException;
import com.healthcare.survey.exception.SurveyClosedException;
import com.healthcare.survey.repository.SurveyLinkRepository;
import com.healthcare.survey.repository.SurveyResponseAnswerRepository;
import com.healthcare.survey.repository.SurveyResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicSurveyService {

    private static final Logger log = LoggerFactory.getLogger(PublicSurveyService.class);

    private final SurveyLinkRepository linkRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyResponseAnswerRepository answerRepository;
    private final ObjectMapper objectMapper;

    public PublicSurveyService(SurveyLinkRepository linkRepository,
                               SurveyResponseRepository responseRepository,
                               SurveyResponseAnswerRepository answerRepository,
                               ObjectMapper objectMapper) {
        this.linkRepository = linkRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PublicSurveyDto getPublicSurvey(String token) {
        SurveyLink link = findAndValidateSurveyLink(token);
        SurveyVersion version = link.getSurveyVersion();

        SurveyTemplateResponse snapshot;
        try {
            snapshot = objectMapper.readValue(version.getPublishedJson(), SurveyTemplateResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse published survey JSON for link token {}", token, e);
            throw new InvalidSurveyException("Survey definition could not be loaded");
        }

        PublicSurveyDto dto = new PublicSurveyDto();
        dto.setToken(link.getPublicToken());
        dto.setReferenceId(snapshot.getReferenceId());
        dto.setName(snapshot.getName());
        dto.setDescription(snapshot.getDescription());
        dto.setCategory(snapshot.getCategory());
        dto.setVersionNumber(version.getVersionNumber());
        dto.setExpiresAt(link.getExpiresAt());
        dto.setMaxResponses(link.getMaxResponses());
        dto.setActive(link.getActive());
        dto.setQuestions(snapshot.getQuestions());

        return dto;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SubmissionResponseDto submitSurveyResponse(String token, SurveySubmissionRequest request) {
        if (request == null) {
            throw new InvalidSurveyException("Submission request cannot be empty");
        }

        SurveyLink link = findAndValidateSurveyLink(token);
        SurveyVersion version = link.getSurveyVersion();

        SurveyTemplateResponse snapshot;
        try {
            snapshot = objectMapper.readValue(version.getPublishedJson(), SurveyTemplateResponse.class);
        } catch (JsonProcessingException e) {
            throw new InvalidSurveyException("Survey definition could not be loaded");
        }

        // Validate submission answers against published version
        validateSubmissionAnswers(snapshot.getQuestions(), request.getAnswers());

        // Create Response
        SurveyResponse response = new SurveyResponse();
        response.setSurveyLink(link);

        if (request.getRespondent() != null) {
            RespondentDetailsDto respondent = request.getRespondent();
            response.setRespondentName(respondent.getName() != null ? respondent.getName().trim() : null);
            response.setMobileNumber(respondent.getMobileNumber() != null ? respondent.getMobileNumber().trim() : null);
            response.setEmailAddress(respondent.getEmailAddress() != null ? respondent.getEmailAddress().trim() : null);
            response.setOverallRating(respondent.getOverallRating());
            response.setOverallComments(respondent.getOverallComments() != null ? respondent.getOverallComments().trim() : null);
        }

        response.setStatus(ResponseStatus.SUBMITTED);
        SurveyResponse savedResponse = responseRepository.save(response);

        // Save Answers
        if (request.getAnswers() != null) {
            for (SubmissionAnswerDto answerDto : request.getAnswers()) {
                if (answerDto.getValue() != null) {
                    SurveyResponseAnswer answer = new SurveyResponseAnswer();
                    answer.setResponse(savedResponse);
                    answer.setQuestionKey(answerDto.getQuestionId().trim());

                    try {
                        answer.setAnswerJson(objectMapper.writeValueAsString(answerDto.getValue()));
                    } catch (JsonProcessingException e) {
                        answer.setAnswerJson(String.valueOf(answerDto.getValue()));
                    }

                    answerRepository.save(answer);
                }
            }
        }

        // Increment response count atomically
        link.setResponseCount(link.getResponseCount() + 1);
        linkRepository.save(link);

        log.info("Survey response submitted successfully: Ref [{}] for link [{}]",
                savedResponse.getReferenceId(), token);

        return new SubmissionResponseDto(
                savedResponse.getReferenceId(),
                "Thank you! Your survey response has been successfully submitted.",
                savedResponse.getSubmittedAt()
        );
    }

    private SurveyLink findAndValidateSurveyLink(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ResourceNotFoundException("Invalid survey link token");
        }

        String trimmed = token.trim();
        SurveyLink link = linkRepository.findByPublicToken(trimmed)
                .or(() -> linkRepository.findByPublicTokenHash(SurveyPublishService.hashToken(trimmed)))
                .orElseThrow(() -> new ResourceNotFoundException("Survey link not found or invalid"));

        if (!Boolean.TRUE.equals(link.getActive())) {
            throw new SurveyClosedException("This survey link has been deactivated by the administrator.");
        }

        if (link.isExpired()) {
            throw new SurveyClosedException("This survey has expired on " + link.getExpiresAt());
        }

        if (link.isLimitReached()) {
            throw new SurveyClosedException("This survey has reached its maximum allowed number of responses.");
        }

        return link;
    }

    private void validateSubmissionAnswers(List<QuestionDefinitionDto> questions, List<SubmissionAnswerDto> answers) {
        Map<String, QuestionDefinitionDto> questionMap = questions.stream()
                .collect(Collectors.toMap(QuestionDefinitionDto::getId, q -> q));

        Map<String, Object> submittedMap = new HashMap<>();
        if (answers != null) {
            for (SubmissionAnswerDto a : answers) {
                if (a.getQuestionId() == null || a.getQuestionId().isBlank()) {
                    throw new InvalidSurveyException("Answer must specify a valid questionId");
                }
                String qId = a.getQuestionId().trim();
                if (!questionMap.containsKey(qId)) {
                    throw new InvalidSurveyException("Unknown question ID in submission: " + qId);
                }
                submittedMap.put(qId, a.getValue());
            }
        }

        // Check required questions and validate types
        for (QuestionDefinitionDto q : questions) {
            String qId = q.getId();
            Object value = submittedMap.get(qId);

            if (Boolean.TRUE.equals(q.getRequired())) {
                if (value == null || (value instanceof String s && s.trim().isEmpty()) || (value instanceof Collection<?> c && c.isEmpty())) {
                    throw new InvalidSurveyException("Missing required answer for question: " + q.getText());
                }
            }

            if (value != null) {
                validateAnswerValue(q, value);
            }
        }
    }

    private void validateAnswerValue(QuestionDefinitionDto q, Object value) {
        switch (q.getType()) {
            case RATING:
                int rating;
                if (value instanceof Number n) {
                    rating = n.intValue();
                } else {
                    try {
                        rating = Integer.parseInt(value.toString().trim());
                    } catch (NumberFormatException e) {
                        throw new InvalidSurveyException("Rating answer must be a number for question: " + q.getText());
                    }
                }
                int min = q.getMin() != null ? q.getMin() : 1;
                int max = q.getMax() != null ? q.getMax() : 5;
                if (rating < min || rating > max) {
                    throw new InvalidSurveyException("Rating value " + rating + " is outside permitted range [" + min + "-" + max + "] for question: " + q.getText());
                }
                break;

            case SINGLE_CHOICE:
            case DROPDOWN:
                String choice = value.toString().trim();
                List<String> options = q.getOptions();
                boolean matches = options.stream().anyMatch(opt -> opt.equalsIgnoreCase(choice));
                if (!matches) {
                    throw new InvalidSurveyException("Option '" + choice + "' is not a valid choice for question: " + q.getText());
                }
                break;

            case MULTIPLE_CHOICE:
                List<?> choices;
                if (value instanceof List<?> l) {
                    choices = l;
                } else {
                    choices = List.of(value.toString());
                }
                List<String> validOptions = q.getOptions();
                for (Object item : choices) {
                    String itemStr = item.toString().trim();
                    boolean optionValid = validOptions.stream().anyMatch(opt -> opt.equalsIgnoreCase(itemStr));
                    if (!optionValid) {
                        throw new InvalidSurveyException("Option '" + itemStr + "' is not a valid choice for question: " + q.getText());
                    }
                }
                break;

            case YES_NO:
                String yn = value.toString().trim().toLowerCase();
                if (!List.of("yes", "no", "true", "false", "1", "0").contains(yn)) {
                    throw new InvalidSurveyException("Yes/No answer must be Yes or No for question: " + q.getText());
                }
                break;

            case NUMBER:
                try {
                    Double.parseDouble(value.toString().trim());
                } catch (NumberFormatException e) {
                    throw new InvalidSurveyException("Answer must be numeric for question: " + q.getText());
                }
                break;

            default:
                break;
        }
    }
}
