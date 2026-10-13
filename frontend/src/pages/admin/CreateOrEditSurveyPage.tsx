import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { adminApi } from '../../api/client';
import { QuestionDefinition, SurveyTemplate } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import { Select } from '../../components/ui/Select';
import { Card, CardContent } from '../../components/ui/Card';
import { QuestionBuilder } from '../../components/survey/QuestionBuilder';
import { useToast } from '../../context/ToastContext';
import {
  Save,
  Rocket,
  ArrowLeft,
  FileCode,
  Layout,
  Eye,
  Sparkles,
} from 'lucide-react';

export const CreateOrEditSurveyPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const isEditMode = Boolean(id);
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState<'visual' | 'json'>('visual');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('PATIENT_CARE');
  const [questions, setQuestions] = useState<QuestionDefinition[]>([
    {
      id: 'q1',
      text: 'How satisfied are you with the waiting time before your consultation?',
      type: 'rating',
      min: 1,
      max: 5,
      required: true,
      helpText: '1 = Very Dissatisfied, 5 = Very Satisfied',
    },
  ]);

  const [jsonText, setJsonText] = useState('');
  const [jsonError, setJsonError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isFetching, setIsFetching] = useState(isEditMode);

  // Sync state to JSON whenever tab switches to JSON
  useEffect(() => {
    if (activeTab === 'json') {
      const templateObj = {
        template: {
          name,
          description,
          category,
          questions,
        },
      };
      setJsonText(JSON.stringify(templateObj, null, 2));
    }
  }, [activeTab, name, description, category, questions]);

  // Handle syncing JSON back to Visual Builder
  const handleJsonChange = (text: string) => {
    setJsonText(text);
    try {
      const parsed = JSON.parse(text);
      const data = parsed.template || parsed;
      if (data.name !== undefined) setName(data.name);
      if (data.description !== undefined) setDescription(data.description);
      if (data.category !== undefined) setCategory(data.category);
      if (Array.isArray(data.questions)) setQuestions(data.questions);
      setJsonError(null);
    } catch (e: any) {
      setJsonError(`JSON Syntax Error: ${e.message}`);
    }
  };

  useEffect(() => {
    if (isEditMode && id) {
      const fetchSurvey = async () => {
        try {
          const data = await adminApi.getSurvey(Number(id));
          setName(data.name);
          setDescription(data.description || '');
          setCategory(data.category || 'PATIENT_CARE');
          setQuestions(data.questions || []);
        } catch (err: any) {
          showToast('Failed to load survey template for editing', 'error');
          navigate('/admin/surveys');
        } finally {
          setIsFetching(false);
        }
      };
      fetchSurvey();
    }
  }, [id, isEditMode, navigate, showToast]);

  const handleSubmit = async (publishImmediately = false) => {
    if (!name.trim()) {
      showToast('Please enter a survey template name', 'error');
      return;
    }
    if (questions.length === 0) {
      showToast('Please add at least one question', 'error');
      return;
    }

    setIsLoading(true);
    try {
      const payload = {
        name: name.trim(),
        description: description.trim(),
        category: category.trim(),
        questions,
      };

      let savedTemplate: SurveyTemplate;
      if (isEditMode && id) {
        savedTemplate = await adminApi.updateSurvey(Number(id), payload);
        showToast('Survey updated successfully', 'success');
      } else {
        savedTemplate = await adminApi.createSurvey(payload);
        showToast('Survey template created', 'success');
      }

      if (publishImmediately) {
        await adminApi.publishSurvey(savedTemplate.id);
        showToast('Survey published successfully!', 'success');
      }

      navigate('/admin/surveys');
    } catch (err: any) {
      showToast(err?.response?.data?.message || 'Failed to save survey', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  if (isFetching) {
    return <div className="p-12 text-center text-slate-400">Loading survey details...</div>;
  }

  return (
    <div className="p-6 md:p-10 space-y-6 max-w-5xl mx-auto w-full">
      {/* Top Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/admin/surveys">
            <Button variant="ghost" size="sm" className="p-2 text-slate-500">
              <ArrowLeft className="w-5 h-5" />
            </Button>
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              {isEditMode ? 'Edit Survey Template' : 'Create Survey Template'}
            </h1>
            <p className="text-xs text-slate-500 mt-0.5">
              Configure survey metadata and dynamic question fields.
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          {isEditMode && id && (
            <Link to={`/admin/surveys/${id}/preview`}>
              <Button variant="outline" size="sm" className="gap-1.5">
                <Eye className="w-4 h-4" /> Preview
              </Button>
            </Link>
          )}
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleSubmit(false)}
            isLoading={isLoading}
            className="gap-1.5"
          >
            <Save className="w-4 h-4" /> Save Draft
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => handleSubmit(true)}
            isLoading={isLoading}
            className="gap-1.5"
          >
            <Rocket className="w-4 h-4" /> Save & Publish
          </Button>
        </div>
      </div>

      {/* Editor Tab Toggle */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-2">
        <button
          type="button"
          onClick={() => setActiveTab('visual')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition-colors ${
            activeTab === 'visual'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Layout className="w-4 h-4" /> Visual Question Builder
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('json')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition-colors ${
            activeTab === 'json'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <FileCode className="w-4 h-4" /> Direct JSON Editor
        </button>
      </div>

      {activeTab === 'visual' ? (
        <div className="space-y-6">
          {/* Metadata Card */}
          <Card>
            <CardContent className="p-6 space-y-4">
              <h3 className="text-base font-semibold text-slate-900 border-b border-slate-100 pb-3">
                General Survey Information
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="md:col-span-2">
                  <Input
                    label="Survey Title"
                    required
                    placeholder="e.g. Hospital Patient Satisfaction Survey"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Category</label>
                  <Select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                  >
                    <option value="PATIENT_CARE">Patient Care & Clinical</option>
                    <option value="OUTPATIENT_OPD">Outpatient (OPD)</option>
                    <option value="INPATIENT_WARD">Inpatient Ward</option>
                    <option value="EMERGENCY_ER">Emergency Room (ER)</option>
                    <option value="GENERAL">General Feedback</option>
                  </Select>
                </div>
              </div>

              <div>
                <Textarea
                  label="Description / Purpose"
                  placeholder="Explain the purpose of this survey to respondents..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={2}
                />
              </div>
            </CardContent>
          </Card>

          {/* Dynamic Question Builder */}
          <QuestionBuilder questions={questions} onChange={setQuestions} />
        </div>
      ) : (
        /* Direct JSON Editor */
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <p className="text-xs text-slate-500">
              Edit the survey specification directly in JSON format. Changes will synchronize with the Visual Builder.
            </p>
            {jsonError ? (
              <span className="text-xs font-semibold text-rose-600">{jsonError}</span>
            ) : (
              <span className="text-xs font-semibold text-emerald-600">✓ Valid JSON</span>
            )}
          </div>
          <textarea
            rows={22}
            value={jsonText}
            onChange={(e) => handleJsonChange(e.target.value)}
            className="w-full font-mono text-xs p-5 bg-slate-900 text-emerald-400 rounded-2xl border border-slate-700 focus:outline-none focus:ring-2 focus:ring-primary-500 resize-y shadow-inner"
          />
        </div>
      )}
    </div>
  );
};
