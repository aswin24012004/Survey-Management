export type QuestionType =
  | 'rating'
  | 'single_choice'
  | 'multiple_choice'
  | 'short_text'
  | 'long_text'
  | 'dropdown'
  | 'yes_no'
  | 'date'
  | 'number'
  | 'boolean';

export interface QuestionDefinition {
  id: string;
  text: string;
  type: QuestionType;
  required: boolean;
  displayOrder?: number;
  min?: number;
  max?: number;
  options?: string[];
  placeholder?: string;
  helpText?: string;
}

export interface SurveyLink {
  id: number;
  token: string;
  surveyUrl: string;
  active: boolean;
  expiresAt?: string;
  maxResponses?: number;
  responseCount: number;
  createdAt: string;
  revokedAt?: string;
  versionNumber?: number;
}

export interface SurveyTemplate {
  id: number;
  referenceId: string;
  name: string;
  description?: string;
  category?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  createdAt: string;
  updatedAt: string;
  createdByUsername?: string;
  questions: QuestionDefinition[];
  activeLink?: SurveyLink;
  versionCount?: number;
  responseCount?: number;
}

export interface SurveySummary {
  id: number;
  referenceId: string;
  name: string;
  description?: string;
  category?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  createdAt: string;
  updatedAt: string;
  questionCount: number;
  responseCount: number;
  activeLinkId?: number;
  activeLinkUrl?: string;
  activeLinkToken?: string;
  activeLinkIsActive?: boolean;
}

export interface PublicSurvey {
  token: string;
  referenceId: string;
  name: string;
  description?: string;
  category?: string;
  versionNumber: number;
  expiresAt?: string;
  maxResponses?: number;
  active: boolean;
  questions: QuestionDefinition[];
}

export interface RespondentDetails {
  name?: string;
  mobileNumber?: string;
  emailAddress?: string;
  overallRating?: number;
  overallComments?: string;
}

export interface SubmissionAnswer {
  questionId: string;
  value: any;
}

export interface SurveySubmissionRequest {
  respondent?: RespondentDetails;
  answers: SubmissionAnswer[];
}

export interface SubmissionResponse {
  confirmationId: string;
  message: string;
  submittedAt: string;
}

export interface SurveyResponseItem {
  id: number;
  referenceId: string;
  surveyLinkId: number;
  respondentName?: string;
  mobileNumber?: string;
  emailAddress?: string;
  overallRating?: number;
  overallComments?: string;
  status: string;
  submittedAt: string;
  answers: Record<string, any>;
}

export interface QuestionStat {
  questionId: string;
  questionText: string;
  questionType: QuestionType;
  totalAnswers: number;
  averageRating?: number;
  choiceDistribution?: Record<string, number>;
}

export interface SurveyAnalytics {
  surveyId: number;
  surveyName: string;
  totalResponses: number;
  averageRating: number;
  questionStats: QuestionStat[];
  ratingDistribution: Record<number, number>;
  timeline: Record<string, number>;
}

export interface RecentSubmission {
  referenceId: string;
  surveyName: string;
  respondentName: string;
  overallRating?: number;
  submittedAt: string;
}

export interface DashboardStats {
  totalSurveys: number;
  publishedSurveys: number;
  draftSurveys: number;
  activeLinks: number;
  totalResponses: number;
  averageRating: number;
  recentSubmissions: RecentSubmission[];
}

export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface AuditLog {
  id: number;
  actorUsername: string;
  action: string;
  entityType: string;
  entityId: string;
  details: string;
  createdAt: string;
}
