import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { publicApi } from '../../api/client';
import { PublicSurvey } from '../../types';
import { DynamicQuestionRenderer } from '../../components/survey/DynamicQuestionRenderer';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import {
  HeartPulse,
  CheckCircle2,
  AlertTriangle,
  Lock,
  Star,
  Send,
  Sparkles,
} from 'lucide-react';

export const PublicSurveyPage: React.FC = () => {
  const { token } = useParams<{ token: string }>();

  const [survey, setSurvey] = useState<PublicSurvey | null>(null);
  const [answers, setAnswers] = useState<Record<string, any>>({});
  const [respondent, setRespondent] = useState({
    name: '',
    mobileNumber: '',
    emailAddress: '',
    overallRating: 5,
    overallComments: '',
  });
  const [consentGiven, setConsentGiven] = useState(true);

  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorStatus, setErrorStatus] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<{
    confirmationId: string;
    message: string;
  } | null>(null);

  useEffect(() => {
    if (!token) return;

    const fetchSurvey = async () => {
      setIsLoading(true);
      setErrorStatus(null);
      try {
        const data = await publicApi.getSurvey(token);
        setSurvey(data);
      } catch (err: any) {
        const msg =
          err?.response?.data?.message ||
          'This survey is currently unavailable, expired, or has reached its response limit.';
        setErrorStatus(msg);
      } finally {
        setIsLoading(false);
      }
    };

    fetchSurvey();
  }, [token]);

  const handleAnswerChange = (questionId: string, value: any) => {
    setAnswers((prev) => ({ ...prev, [questionId]: value }));
    if (validationErrors[questionId]) {
      setValidationErrors((prev) => {
        const copy = { ...prev };
        delete copy[questionId];
        return copy;
      });
    }
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};

    if (!survey) return false;

    // Validate questions
    for (const q of survey.questions) {
      const val = answers[q.id];
      if (q.required) {
        if (
          val === undefined ||
          val === null ||
          val === '' ||
          (Array.isArray(val) && val.length === 0)
        ) {
          errors[q.id] = 'This question is required. Please provide an answer.';
        }
      }
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm() || !token) {
      window.scrollTo({ top: 300, behavior: 'smooth' });
      return;
    }

    setIsSubmitting(true);
    try {
      const formattedAnswers = Object.entries(answers).map(([qId, val]) => ({
        questionId: qId,
        value: val,
      }));

      const payload = {
        respondent: {
          name: respondent.name.trim() || undefined,
          mobileNumber: respondent.mobileNumber.trim() || undefined,
          emailAddress: respondent.emailAddress.trim() || undefined,
          overallRating: respondent.overallRating,
          overallComments: respondent.overallComments.trim() || undefined,
        },
        answers: formattedAnswers,
      };

      const result = await publicApi.submitResponse(token, payload);
      setConfirmation({
        confirmationId: result.confirmationId,
        message: result.message,
      });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'Failed to submit response. Please try again.';
      alert(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-primary-600/10 text-primary-600 flex items-center justify-center mx-auto animate-pulse">
            <HeartPulse className="w-7 h-7" />
          </div>
          <p className="text-sm font-semibold text-slate-700">Loading Healthcare Survey...</p>
        </div>
      </div>
    );
  }

  // Survey Expired or Unavailable Screen
  if (errorStatus || !survey) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-3xl p-8 border border-slate-200 shadow-xl text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
            <AlertTriangle className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-slate-900">Survey Unavailable</h2>
          <p className="text-sm text-slate-600 leading-relaxed">
            {errorStatus || 'This survey link is inactive or has reached its maximum responses.'}
          </p>
          <div className="pt-4 border-t border-slate-100 text-xs text-slate-400">
            Healthcare Quality Management System (QMS)
          </div>
        </div>
      </div>
    );
  }

  // Submission Confirmation Screen
  if (confirmation) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="max-w-lg w-full bg-white rounded-3xl p-8 sm:p-10 border border-slate-200/90 shadow-2xl text-center space-y-6 animate-in zoom-in-95 duration-200">
          <div className="w-20 h-20 rounded-3xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto shadow-lg shadow-emerald-500/10">
            <CheckCircle2 className="w-10 h-10" />
          </div>
          <div className="space-y-2">
            <h2 className="text-2xl font-bold text-slate-900">Response Submitted!</h2>
            <p className="text-sm text-slate-600 leading-relaxed">{confirmation.message}</p>
          </div>

          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200 text-left space-y-2">
            <div className="flex justify-between items-center text-xs">
              <span className="text-slate-500">Confirmation Reference ID:</span>
              <span className="font-mono font-bold text-primary-700 bg-primary-50 px-2 py-0.5 rounded border border-primary-200">
                {confirmation.confirmationId}
              </span>
            </div>
            <div className="flex justify-between items-center text-xs">
              <span className="text-slate-500">Survey:</span>
              <span className="font-medium text-slate-900">{survey.name}</span>
            </div>
          </div>

          <p className="text-xs text-slate-400">
            Your valuable feedback directly contributes to enhancing patient care and hospital service excellence.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-100 to-slate-50 py-8 px-4 sm:px-6">
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Survey Banner Header */}
        <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/90 shadow-md space-y-3 relative overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-primary-600 via-primary-500 to-indigo-600" />
          <div className="flex items-center gap-2.5 text-primary-600 pt-2">
            <HeartPulse className="w-6 h-6" />
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">
              Healthcare Patient Feedback
            </span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            {survey.name}
          </h1>
          {survey.description && (
            <p className="text-sm text-slate-600 leading-relaxed">{survey.description}</p>
          )}

          <div className="pt-3 border-t border-slate-100 flex items-center gap-2 text-xs text-slate-400">
            <Lock className="w-3.5 h-3.5 text-slate-400" />
            <span>Secure & confidential. Responses are stored for quality improvement purposes.</span>
          </div>
        </div>

        {/* Survey Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Respondent Details (Configurable & Optional for anonymous feedback) */}
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/90 shadow-sm space-y-4">
            <div className="border-b border-slate-100 pb-3">
              <h2 className="text-base font-bold text-slate-900">Respondent Information (Optional)</h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Leave blank if you prefer to submit this survey completely anonymously.
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <Input
                  label="Full Name"
                  placeholder="e.g. Jane Doe"
                  value={respondent.name}
                  onChange={(e) => setRespondent({ ...respondent, name: e.target.value })}
                />
              </div>
              <div>
                <Input
                  label="Mobile Number"
                  placeholder="e.g. 9876543210"
                  value={respondent.mobileNumber}
                  onChange={(e) => setRespondent({ ...respondent, mobileNumber: e.target.value })}
                />
              </div>
              <div className="sm:col-span-2">
                <Input
                  label="Email Address"
                  type="email"
                  placeholder="e.g. jane@example.com"
                  value={respondent.emailAddress}
                  onChange={(e) => setRespondent({ ...respondent, emailAddress: e.target.value })}
                />
              </div>
            </div>

            {/* Overall Experience Rating */}
            <div className="pt-2">
              <label className="block text-sm font-semibold text-slate-900 mb-2">
                Overall Hospital Visit Experience
              </label>
              <div className="flex items-center gap-2">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setRespondent({ ...respondent, overallRating: star })}
                    className={`flex items-center justify-center w-11 h-11 rounded-xl text-sm font-bold border transition-all ${
                      respondent.overallRating === star
                        ? 'bg-amber-400 text-white border-amber-400 shadow-md scale-105 ring-2 ring-amber-200'
                        : 'bg-white text-slate-700 border-slate-200 hover:bg-amber-50'
                    }`}
                  >
                    <span className="flex items-center gap-0.5">
                      {star}
                      <Star className={`w-3.5 h-3.5 ${respondent.overallRating === star ? 'fill-white text-white' : 'text-amber-400 fill-amber-400'}`} />
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <div className="pt-2">
              <Textarea
                label="Overall Remarks or Care Team Compliments"
                placeholder="Share any general thoughts regarding your care experience..."
                rows={2}
                value={respondent.overallComments}
                onChange={(e) => setRespondent({ ...respondent, overallComments: e.target.value })}
              />
            </div>
          </div>

          {/* Dynamic Questions Section */}
          <div className="space-y-4">
            <h2 className="text-base font-bold text-slate-900 px-1">
              Survey Questions ({survey.questions.length})
            </h2>

            {survey.questions.map((q) => (
              <DynamicQuestionRenderer
                key={q.id}
                question={q}
                value={answers[q.id]}
                error={validationErrors[q.id]}
                onChange={(val) => handleAnswerChange(q.id, val)}
              />
            ))}
          </div>

          {/* Consent and Submit Button */}
          <div className="bg-white rounded-3xl p-6 border border-slate-200/90 shadow-sm space-y-4">
            <label className="flex items-start gap-3 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={consentGiven}
                onChange={(e) => setConsentGiven(e.target.checked)}
                className="mt-0.5 h-4 w-4 rounded text-primary-600 border-slate-300 focus:ring-primary-500"
              />
              <span className="text-xs text-slate-600 leading-relaxed">
                I agree that my feedback may be processed for quality improvement and healthcare safety analytics under standard data protection guidelines.
              </span>
            </label>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              isLoading={isSubmitting}
              disabled={!consentGiven}
              className="w-full text-base font-semibold py-3.5 rounded-2xl shadow-xl shadow-primary-600/20 gap-2"
            >
              <Send className="w-5 h-5" />
              Submit Survey Response
            </Button>
          </div>
        </form>

        <p className="text-center text-xs text-slate-400 pt-2 pb-6">
          Powered by Healthcare QMS Dynamic Survey Platform
        </p>
      </div>
    </div>
  );
};
