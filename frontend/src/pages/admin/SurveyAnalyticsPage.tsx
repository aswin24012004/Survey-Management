import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { adminApi } from '../../api/client';
import { SurveyAnalytics } from '../../types';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useToast } from '../../context/ToastContext';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
} from 'recharts';
import {
  ArrowLeft,
  Star,
  Users,
  Download,
  Activity,
  CheckCircle2,
  Calendar,
} from 'lucide-react';

export const SurveyAnalyticsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { showToast } = useToast();
  const [analytics, setAnalytics] = useState<SurveyAnalytics | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (id) {
      const fetchAnalytics = async () => {
        try {
          const data = await adminApi.getSurveyAnalytics(Number(id));
          setAnalytics(data);
        } catch (err: any) {
          showToast('Failed to load survey analytics', 'error');
        } finally {
          setIsLoading(false);
        }
      };
      fetchAnalytics();
    }
  }, [id, showToast]);

  const handleExportCsv = async () => {
    if (!id || !analytics) return;
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
      showToast('Failed to export CSV', 'error');
    }
  };

  if (isLoading) {
    return <div className="p-12 text-center text-slate-400">Loading analytics...</div>;
  }

  if (!analytics) {
    return <div className="p-12 text-center text-rose-500">Analytics not found</div>;
  }

  // Format rating distribution for Recharts
  const ratingChartData = [1, 2, 3, 4, 5].map((star) => ({
    name: `${star} Star${star > 1 ? 's' : ''}`,
    count: analytics.ratingDistribution[star] || 0,
  }));

  // Format timeline for Recharts
  const timelineChartData = Object.entries(analytics.timeline || {}).map(([date, count]) => ({
    date,
    submissions: count,
  }));

  return (
    <div className="p-6 md:p-10 space-y-8 max-w-7xl mx-auto w-full">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/admin/surveys">
            <Button variant="ghost" size="sm" className="p-2 text-slate-500">
              <ArrowLeft className="w-5 h-5" />
            </Button>
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Survey Analytics</h1>
            <p className="text-xs text-slate-500 mt-0.5">{analytics.surveyName}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Link to={`/admin/surveys/${id}/responses`}>
            <Button variant="outline" size="sm">
              View All Responses
            </Button>
          </Link>
          {analytics.totalResponses > 0 && (
            <Button variant="primary" size="sm" onClick={handleExportCsv} className="gap-2">
              <Download className="w-4 h-4" /> Export CSV
            </Button>
          )}
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500">Total Responses</p>
              <p className="text-2xl font-bold text-slate-900 mt-1">{analytics.totalResponses}</p>
            </div>
            <div className="p-3 bg-primary-50 text-primary-600 rounded-xl">
              <Users className="w-6 h-6" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500">Average Overall Rating</p>
              <div className="flex items-center gap-1.5 mt-1">
                <span className="text-2xl font-bold text-slate-900">{analytics.averageRating}</span>
                <span className="text-xs text-slate-400">/ 5.0</span>
              </div>
            </div>
            <div className="p-3 bg-amber-50 text-amber-500 rounded-xl">
              <Star className="w-6 h-6 fill-amber-400" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500">Questions Analyzed</p>
              <p className="text-2xl font-bold text-slate-900 mt-1">{analytics.questionStats.length}</p>
            </div>
            <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl">
              <Activity className="w-6 h-6" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500">Feedback Quality</p>
              <p className="text-base font-bold text-emerald-600 mt-1 flex items-center gap-1">
                <CheckCircle2 className="w-4 h-4" /> Validated QMS Data
              </p>
            </div>
            <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
              <Calendar className="w-6 h-6" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Visual Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Rating Breakdown Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Overall Rating Distribution</CardTitle>
            <CardDescription>Frequency of respondent star ratings</CardDescription>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={ratingChartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#64748b' }} axisLine={false} tickLine={false} />
                <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: '#64748b' }} axisLine={false} tickLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderRadius: '12px', border: 'none', color: '#fff', fontSize: '12px' }}
                />
                <Bar dataKey="count" fill="#026fc7" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Timeline Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Submission Trends</CardTitle>
            <CardDescription>Daily response collection progression</CardDescription>
          </CardHeader>
          <CardContent className="h-72">
            {timelineChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={timelineChartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#64748b' }} axisLine={false} tickLine={false} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: '#64748b' }} axisLine={false} tickLine={false} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0f172a', borderRadius: '12px', border: 'none', color: '#fff', fontSize: '12px' }}
                  />
                  <Line type="monotone" dataKey="submissions" stroke="#10b981" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-slate-400">
                No timeline data available yet.
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Question-wise Statistics Breakdown */}
      <div className="space-y-4">
        <h3 className="text-lg font-bold text-slate-900 tracking-tight">Question-wise Analysis</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {analytics.questionStats.map((q) => (
            <Card key={q.questionId} className="border-slate-200">
              <CardContent className="p-5 space-y-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <span className="text-[10px] font-bold tracking-wider uppercase text-primary-600 bg-primary-50 px-2 py-0.5 rounded">
                      {q.questionType}
                    </span>
                    <h4 className="text-sm font-semibold text-slate-900 mt-1">{q.questionText}</h4>
                  </div>
                  <span className="text-xs font-semibold text-slate-500 whitespace-nowrap">
                    {q.totalAnswers} answers
                  </span>
                </div>

                {q.questionType === 'rating' && q.averageRating !== undefined && (
                  <div className="space-y-2 pt-2">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-slate-600 font-medium">Average Score</span>
                      <span className="font-bold text-amber-500 flex items-center gap-1">
                        <Star className="w-3.5 h-3.5 fill-amber-400" />
                        {q.averageRating} / 5.0
                      </span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                      <div
                        className="bg-amber-400 h-2.5 rounded-full"
                        style={{ width: `${(q.averageRating / 5) * 100}%` }}
                      />
                    </div>
                  </div>
                )}

                {q.choiceDistribution && Object.keys(q.choiceDistribution).length > 0 && (
                  <div className="space-y-2 pt-1 border-t border-slate-100">
                    <span className="text-xs font-semibold text-slate-500">Choice Distribution:</span>
                    {Object.entries(q.choiceDistribution).map(([choice, count]) => {
                      const pct = q.totalAnswers > 0 ? Math.round((count / q.totalAnswers) * 100) : 0;
                      return (
                        <div key={choice} className="space-y-1">
                          <div className="flex justify-between text-xs text-slate-700">
                            <span className="truncate pr-2">{choice}</span>
                            <span className="font-semibold text-slate-900">{count} ({pct}%)</span>
                          </div>
                          <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                            <div
                              className="bg-primary-600 h-2 rounded-full"
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
};
