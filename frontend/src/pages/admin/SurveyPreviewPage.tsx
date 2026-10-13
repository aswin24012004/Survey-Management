import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { adminApi } from '../../api/client';
import { SurveyTemplate } from '../../types';
import { Button } from '../../components/ui/Button';
import { DynamicQuestionRenderer } from '../../components/survey/DynamicQuestionRenderer';
import { PublishModal } from '../../components/survey/PublishModal';
import { ArrowLeft, Smartphone, Monitor, Rocket, Edit, HeartPulse } from 'lucide-react';

export const SurveyPreviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [survey, setSurvey] = useState<SurveyTemplate | null>(null);
  const [answers, setAnswers] = useState<Record<string, any>>({});
  const [viewMode, setViewMode] = useState<'desktop' | 'mobile'>('desktop');
  const [isPublishModalOpen, setIsPublishModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (id) {
      const fetchSurvey = async () => {
        try {
          const data = await adminApi.getSurvey(Number(id));
          setSurvey(data);
        } catch (err) {
          console.error('Failed to load survey preview', err);
        } finally {
          setIsLoading(false);
        }
      };
      fetchSurvey();
    }
  }, [id]);

  if (isLoading) {
    return <div className="p-12 text-center text-slate-400">Loading survey preview...</div>;
  }

  if (!survey) {
    return <div className="p-12 text-center text-rose-500">Survey not found</div>;
  }

  return (
    <div className="p-6 md:p-10 space-y-6 max-w-5xl mx-auto w-full">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/admin/surveys">
            <Button variant="ghost" size="sm" className="p-2 text-slate-500">
              <ArrowLeft className="w-5 h-5" />
            </Button>
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs px-2 py-0.5 rounded-md bg-amber-100 text-amber-800 font-semibold">
                PREVIEW MODE
              </span>
              <h1 className="text-xl font-bold text-slate-900">{survey.name}</h1>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Simulating respondent experience. Answers in preview mode are not saved.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Device viewport toggle */}
          <div className="bg-slate-200/80 p-1 rounded-xl flex items-center gap-1">
            <button
              onClick={() => setViewMode('desktop')}
              className={`p-1.5 rounded-lg text-xs font-medium transition-colors ${
                viewMode === 'desktop' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600'
              }`}
              title="Desktop View"
            >
              <Monitor className="w-4 h-4" />
            </button>
            <button
              onClick={() => setViewMode('mobile')}
              className={`p-1.5 rounded-lg text-xs font-medium transition-colors ${
                viewMode === 'mobile' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600'
              }`}
              title="Mobile View"
            >
              <Smartphone className="w-4 h-4" />
            </button>
          </div>

          <Link to={`/admin/surveys/${survey.id}/edit`}>
            <Button variant="outline" size="sm" className="gap-1.5">
              <Edit className="w-4 h-4" /> Edit
            </Button>
          </Link>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsPublishModalOpen(true)}
            className="gap-1.5"
          >
            <Rocket className="w-4 h-4" /> Publish
          </Button>
        </div>
      </div>

      {/* Preview Container Frame */}
      <div className="flex justify-center transition-all">
        <div
          className={`w-full transition-all ${
            viewMode === 'mobile'
              ? 'max-w-sm border-[8px] border-slate-800 rounded-[36px] p-2 bg-slate-900 shadow-2xl'
              : 'max-w-3xl'
          }`}
        >
          <div className="bg-slate-50 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm border border-slate-200 overflow-hidden">
            {/* Survey Header Banner */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
              <div className="flex items-center gap-2.5 text-primary-600">
                <HeartPulse className="w-6 h-6" />
                <span className="font-bold text-xs uppercase tracking-wider text-slate-400">
                  Healthcare Quality Assessment
                </span>
              </div>
              <h2 className="text-xl font-bold text-slate-900">{survey.name}</h2>
              {survey.description && (
                <p className="text-sm text-slate-600 leading-relaxed">{survey.description}</p>
              )}
            </div>

            {/* Respondent Fields Sample in Preview */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
              <h3 className="text-sm font-semibold text-slate-800 pb-2 border-b border-slate-100">
                Respondent Information
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-slate-400 italic">
                <div className="p-3 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                  Full Name (Optional)
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                  Mobile Number (Optional)
                </div>
              </div>
            </div>

            {/* Render Questions */}
            <div className="space-y-4">
              {survey.questions.map((q) => (
                <DynamicQuestionRenderer
                  key={q.id}
                  question={q}
                  value={answers[q.id]}
                  onChange={(val) => setAnswers((prev) => ({ ...prev, [q.id]: val }))}
                />
              ))}
            </div>

            <div className="pt-2">
              <Button variant="primary" size="lg" className="w-full shadow-lg" disabled>
                Submit Survey (Preview Mode)
              </Button>
            </div>
          </div>
        </div>
      </div>

      <PublishModal
        isOpen={isPublishModalOpen}
        onClose={() => setIsPublishModalOpen(false)}
        surveyId={survey.id}
        surveyName={survey.name}
        initialLinkUrl={survey.activeLink?.surveyUrl}
        initialToken={survey.activeLink?.token}
        initialIsActive={survey.activeLink?.active}
      />
    </div>
  );
};
