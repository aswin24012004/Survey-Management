import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { adminApi } from '../../api/client';
import { SurveyResponseItem, SurveyTemplate } from '../../types';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import {
  ArrowLeft,
  Download,
  Star,
  Clock,
  Eye,
  User,
  Phone,
  Mail,
  Search,
  CheckCircle2,
} from 'lucide-react';

export const SurveyResponsesPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { showToast } = useToast();
  const [survey, setSurvey] = useState<SurveyTemplate | null>(null);
  const [responses, setResponses] = useState<SurveyResponseItem[]>([]);
  const [selectedResponse, setSelectedResponse] = useState<SurveyResponseItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchData = async () => {
    if (!id) return;
    setIsLoading(true);
    try {
      const [surveyData, responseList] = await Promise.all([
        adminApi.getSurvey(Number(id)),
        adminApi.getSurveyResponses(Number(id)),
      ]);
      setSurvey(surveyData);
      setResponses(responseList);
    } catch (err: any) {
      showToast('Failed to load responses', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleExportCsv = async () => {
    if (!id || !survey) return;
    try {
      const blob = await adminApi.exportSurveyResponses(Number(id));
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `survey_${id}_responses.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      showToast('Responses exported to CSV', 'success');
    } catch (err: any) {
      showToast('Failed to export responses', 'error');
    }
  };

  const filteredResponses = responses.filter((r) => {
    const q = search.toLowerCase();
    return (
      r.referenceId.toLowerCase().includes(q) ||
      (r.respondentName && r.respondentName.toLowerCase().includes(q)) ||
      (r.emailAddress && r.emailAddress.toLowerCase().includes(q)) ||
      (r.mobileNumber && r.mobileNumber.includes(q))
    );
  });

  if (isLoading) {
    return <div className="p-12 text-center text-slate-400">Loading responses...</div>;
  }

  return (
    <div className="p-6 md:p-10 space-y-6 max-w-7xl mx-auto w-full">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/admin/surveys">
            <Button variant="ghost" size="sm" className="p-2 text-slate-500">
              <ArrowLeft className="w-5 h-5" />
            </Button>
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Survey Responses</h1>
            <p className="text-xs text-slate-500 mt-0.5">
              {survey?.name} ({responses.length} total submissions)
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Link to={`/admin/surveys/${id}/analytics`}>
            <Button variant="outline" size="sm">
              View Analytics
            </Button>
          </Link>
          {responses.length > 0 && (
            <Button variant="primary" size="sm" onClick={handleExportCsv} className="gap-2">
              <Download className="w-4 h-4" /> Export CSV
            </Button>
          )}
        </div>
      </div>

      {/* Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            placeholder="Search by respondent name, ref ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
      </div>

      {/* Responses Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        {filteredResponses.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50/70 text-slate-500 text-xs font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3.5">Reference ID</th>
                  <th className="px-6 py-3.5">Respondent</th>
                  <th className="px-6 py-3.5">Contact</th>
                  <th className="px-6 py-3.5">Overall Rating</th>
                  <th className="px-6 py-3.5">Submitted At</th>
                  <th className="px-6 py-3.5 text-right">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredResponses.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-6 py-4 font-mono font-semibold text-xs text-primary-700">
                      {item.referenceId}
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-900">
                      {item.respondentName || <span className="text-slate-400 italic">Anonymous</span>}
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-600">
                      <div>{item.emailAddress || '-'}</div>
                      <div className="text-slate-400">{item.mobileNumber}</div>
                    </td>
                    <td className="px-6 py-4">
                      {item.overallRating ? (
                        <div className="flex items-center gap-1 text-amber-500 font-semibold text-xs">
                          <Star className="w-3.5 h-3.5 fill-amber-400" />
                          {item.overallRating} / 5
                        </div>
                      ) : (
                        <span className="text-slate-400 text-xs">-</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-slate-500 text-xs flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      {new Date(item.submittedAt).toLocaleString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedResponse(item)}
                        className="gap-1 text-primary-600 hover:text-primary-800"
                      >
                        <Eye className="w-4 h-4" /> View Answers
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-16 text-center text-slate-400">
            No submitted responses found for this survey yet.
          </div>
        )}
      </div>

      {/* Response Answers Modal */}
      {selectedResponse && (
        <Modal
          isOpen={Boolean(selectedResponse)}
          onClose={() => setSelectedResponse(null)}
          title={`Response: ${selectedResponse.referenceId}`}
          description={`Submitted on ${new Date(selectedResponse.submittedAt).toLocaleString()}`}
          maxWidth="2xl"
        >
          <div className="space-y-6">
            {/* Respondent Bio Card */}
            <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200 grid grid-cols-2 gap-3 text-xs">
              <div>
                <span className="text-slate-400 block">Respondent Name</span>
                <span className="font-semibold text-slate-900">
                  {selectedResponse.respondentName || 'Anonymous'}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block">Overall Rating</span>
                <span className="font-semibold text-amber-600">
                  {selectedResponse.overallRating ? `${selectedResponse.overallRating} / 5 Stars` : 'Not provided'}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block">Email Address</span>
                <span className="font-medium text-slate-700">
                  {selectedResponse.emailAddress || 'Not provided'}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block">Mobile Number</span>
                <span className="font-medium text-slate-700">
                  {selectedResponse.mobileNumber || 'Not provided'}
                </span>
              </div>
              {selectedResponse.overallComments && (
                <div className="col-span-2 pt-2 border-t border-slate-200">
                  <span className="text-slate-400 block">Overall Comments</span>
                  <p className="text-slate-800 font-medium italic mt-0.5">
                    "{selectedResponse.overallComments}"
                  </p>
                </div>
              )}
            </div>

            {/* Questions & Answers */}
            <div className="space-y-4">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                Individual Question Answers
              </h4>
              <div className="space-y-3">
                {survey?.questions.map((q, idx) => {
                  const ans = selectedResponse.answers[q.id];
                  return (
                    <div
                      key={q.id}
                      className="p-4 bg-white border border-slate-200 rounded-xl space-y-1.5"
                    >
                      <div className="flex items-center gap-2">
                        <span className="w-5 h-5 rounded-full bg-slate-100 text-slate-600 font-bold text-[10px] flex items-center justify-center">
                          {idx + 1}
                        </span>
                        <p className="text-xs font-semibold text-slate-800">{q.text}</p>
                      </div>
                      <div className="pl-7">
                        {ans !== undefined && ans !== null && ans !== '' ? (
                          <div className="text-sm font-semibold text-primary-700">
                            {Array.isArray(ans) ? (
                              <div className="flex flex-wrap gap-1 mt-1">
                                {ans.map((a, i) => (
                                  <span key={i} className="px-2 py-0.5 bg-primary-50 rounded-md text-xs font-medium text-primary-800 border border-primary-200">
                                    {String(a)}
                                  </span>
                                ))}
                              </div>
                            ) : q.type === 'rating' ? (
                              <div className="flex items-center gap-1 text-amber-500">
                                <Star className="w-4 h-4 fill-amber-400" />
                                <span>{ans} / {q.max || 5}</span>
                              </div>
                            ) : (
                              <span>{String(ans)}</span>
                            )}
                          </div>
                        ) : (
                          <span className="text-xs text-slate-400 italic">No answer provided</span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="flex justify-end pt-2">
              <Button variant="outline" onClick={() => setSelectedResponse(null)}>
                Close
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
