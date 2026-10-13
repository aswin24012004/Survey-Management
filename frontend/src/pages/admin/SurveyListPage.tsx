import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../../api/client';
import { SurveySummary } from '../../types';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Input } from '../../components/ui/Input';
import { PublishModal } from '../../components/survey/PublishModal';
import { JsonImportModal } from '../../components/survey/JsonImportModal';
import { useToast } from '../../context/ToastContext';
import {
  PlusCircle,
  FileJson,
  Search,
  Eye,
  Edit,
  Share2,
  BarChart3,
  MessageSquare,
  Download,
  Archive,
  CheckCircle2,
  Clock,
  ExternalLink,
} from 'lucide-react';

export const SurveyListPage: React.FC = () => {
  const { showToast } = useToast();
  const [surveys, setSurveys] = useState<SurveySummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'>('ALL');

  // Modal states
  const [publishModalData, setPublishModalData] = useState<{
    isOpen: boolean;
    surveyId: number;
    surveyName: string;
    linkUrl?: string;
    token?: string;
    isActive?: boolean;
  }>({
    isOpen: false,
    surveyId: 0,
    surveyName: '',
  });

  const [isImportModalOpen, setIsImportModalOpen] = useState(false);

  const fetchSurveys = async () => {
    setIsLoading(true);
    try {
      const data = await adminApi.getSurveys();
      setSurveys(data);
    } catch (err) {
      console.error('Failed to load surveys', err);
      showToast('Failed to load surveys', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchSurveys();
  }, []);

  const handleArchive = async (id: number, name: string) => {
    if (!window.confirm(`Are you sure you want to archive "${name}"? Active links will be closed.`)) {
      return;
    }
    try {
      await adminApi.archiveSurvey(id);
      showToast('Survey archived successfully', 'success');
      fetchSurveys();
    } catch (err: any) {
      showToast(err?.response?.data?.message || 'Failed to archive survey', 'error');
    }
  };

  const handleExportCsv = async (id: number, name: string) => {
    try {
      const blob = await adminApi.exportSurveyResponses(id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `survey_${id}_responses.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      showToast(`Exported responses for "${name}"`, 'success');
    } catch (err: any) {
      showToast(err?.response?.data?.message || 'Failed to export CSV', 'error');
    }
  };

  const filteredSurveys = surveys.filter((s) => {
    const matchesSearch =
      s.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (s.category && s.category.toLowerCase().includes(searchTerm.toLowerCase())) ||
      s.referenceId.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || s.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="p-6 md:p-10 space-y-6 max-w-7xl mx-auto w-full">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Survey Management</h1>
          <p className="text-sm text-slate-500 mt-1">
            Build, publish, monitor, and export dynamic healthcare quality surveys.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => setIsImportModalOpen(true)}
            className="gap-2"
          >
            <FileJson className="w-4 h-4 text-primary-600" />
            Import JSON
          </Button>
          <Link to="/admin/surveys/new">
            <Button variant="primary" className="gap-2">
              <PlusCircle className="w-4 h-4" />
              New Survey
            </Button>
          </Link>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            placeholder="Search surveys by title or category..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>

        <div className="flex items-center gap-1.5 self-start sm:self-auto overflow-x-auto w-full sm:w-auto">
          {(['ALL', 'PUBLISHED', 'DRAFT', 'ARCHIVED'] as const).map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors ${
                statusFilter === st
                  ? 'bg-slate-900 text-white'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-12 text-center text-slate-400">Loading surveys...</div>
        ) : filteredSurveys.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50/70 text-slate-500 text-xs font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3.5">Survey Title</th>
                  <th className="px-6 py-3.5">Category</th>
                  <th className="px-6 py-3.5">Status</th>
                  <th className="px-6 py-3.5">Questions</th>
                  <th className="px-6 py-3.5">Responses</th>
                  <th className="px-6 py-3.5">Public Link</th>
                  <th className="px-6 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredSurveys.map((survey) => (
                  <tr key={survey.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-6 py-4">
                      <div>
                        <Link
                          to={`/admin/surveys/${survey.id}/preview`}
                          className="font-semibold text-slate-900 hover:text-primary-600 transition-colors"
                        >
                          {survey.name}
                        </Link>
                        <p className="text-xs text-slate-400 font-mono mt-0.5">{survey.referenceId}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs font-medium text-slate-600 bg-slate-100 px-2 py-1 rounded-md">
                        {survey.category || 'GENERAL'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <Badge
                        variant={
                          survey.status === 'PUBLISHED'
                            ? 'success'
                            : survey.status === 'ARCHIVED'
                            ? 'danger'
                            : 'default'
                        }
                      >
                        {survey.status}
                      </Badge>
                    </td>
                    <td className="px-6 py-4 text-slate-700 font-medium">
                      {survey.questionCount}
                    </td>
                    <td className="px-6 py-4 text-slate-700 font-medium">
                      {survey.responseCount}
                    </td>
                    <td className="px-6 py-4">
                      {survey.activeLinkUrl ? (
                        <button
                          onClick={() =>
                            setPublishModalData({
                              isOpen: true,
                              surveyId: survey.id,
                              surveyName: survey.name,
                              linkUrl: survey.activeLinkUrl,
                              token: survey.activeLinkToken,
                              isActive: survey.activeLinkIsActive,
                            })
                          }
                          className="inline-flex items-center gap-1.5 text-xs text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-200 hover:bg-emerald-100 font-medium"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                          Link Active
                        </button>
                      ) : (
                        <button
                          onClick={() =>
                            setPublishModalData({
                              isOpen: true,
                              surveyId: survey.id,
                              surveyName: survey.name,
                            })
                          }
                          className="inline-flex items-center gap-1 text-xs text-slate-500 bg-slate-100 px-2 py-1 rounded-lg hover:bg-slate-200 font-medium"
                        >
                          Publish Now
                        </button>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Link to={`/admin/surveys/${survey.id}/preview`}>
                          <Button variant="ghost" size="sm" title="Preview Survey" className="p-1.5 text-slate-500 hover:text-slate-800">
                            <Eye className="w-4 h-4" />
                          </Button>
                        </Link>
                        {survey.status !== 'ARCHIVED' && (
                          <Link to={`/admin/surveys/${survey.id}/edit`}>
                            <Button variant="ghost" size="sm" title="Edit Survey" className="p-1.5 text-slate-500 hover:text-slate-800">
                              <Edit className="w-4 h-4" />
                            </Button>
                          </Link>
                        )}
                        <Button
                          variant="ghost"
                          size="sm"
                          title="Share / Manage Link"
                          onClick={() =>
                            setPublishModalData({
                              isOpen: true,
                              surveyId: survey.id,
                              surveyName: survey.name,
                              linkUrl: survey.activeLinkUrl,
                              token: survey.activeLinkToken,
                              isActive: survey.activeLinkIsActive,
                            })
                          }
                          className="p-1.5 text-primary-600 hover:text-primary-800"
                        >
                          <Share2 className="w-4 h-4" />
                        </Button>
                        <Link to={`/admin/surveys/${survey.id}/responses`}>
                          <Button variant="ghost" size="sm" title="Responses" className="p-1.5 text-slate-500 hover:text-slate-800">
                            <MessageSquare className="w-4 h-4" />
                          </Button>
                        </Link>
                        <Link to={`/admin/surveys/${survey.id}/analytics`}>
                          <Button variant="ghost" size="sm" title="Analytics" className="p-1.5 text-slate-500 hover:text-slate-800">
                            <BarChart3 className="w-4 h-4" />
                          </Button>
                        </Link>
                        {survey.responseCount > 0 && (
                          <Button
                            variant="ghost"
                            size="sm"
                            title="Export CSV"
                            onClick={() => handleExportCsv(survey.id, survey.name)}
                            className="p-1.5 text-emerald-600 hover:text-emerald-800"
                          >
                            <Download className="w-4 h-4" />
                          </Button>
                        )}
                        {survey.status !== 'ARCHIVED' && (
                          <Button
                            variant="ghost"
                            size="sm"
                            title="Archive Survey"
                            onClick={() => handleArchive(survey.id, survey.name)}
                            className="p-1.5 text-rose-500 hover:text-rose-700"
                          >
                            <Archive className="w-4 h-4" />
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-16 text-center text-slate-500">
            <p className="text-base font-semibold text-slate-700">No surveys found</p>
            <p className="text-xs text-slate-400 mt-1">
              Create a new survey using the visual builder or import template JSON.
            </p>
          </div>
        )}
      </div>

      {/* Publish & Link Modal */}
      <PublishModal
        isOpen={publishModalData.isOpen}
        onClose={() => setPublishModalData((prev) => ({ ...prev, isOpen: false }))}
        surveyId={publishModalData.surveyId}
        surveyName={publishModalData.surveyName}
        initialLinkUrl={publishModalData.linkUrl}
        initialToken={publishModalData.token}
        initialIsActive={publishModalData.isActive}
        onPublished={fetchSurveys}
      />

      {/* JSON Import Modal */}
      <JsonImportModal
        isOpen={isImportModalOpen}
        onClose={() => setIsImportModalOpen(false)}
        onImportSuccess={fetchSurveys}
      />
    </div>
  );
};
