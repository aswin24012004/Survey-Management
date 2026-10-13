import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../../api/client';
import { DashboardStats } from '../../types';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import {
  FileSpreadsheet,
  CheckCircle2,
  Link2,
  Users,
  Star,
  PlusCircle,
  ArrowRight,
  Clock,
  Sparkles,
} from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await adminApi.getDashboardStats();
        setStats(data);
      } catch (err) {
        console.error('Failed to load dashboard statistics', err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchStats();
  }, []);

  const kpis = [
    {
      title: 'Total Surveys',
      value: stats?.totalSurveys ?? 0,
      icon: FileSpreadsheet,
      color: 'text-primary-600 bg-primary-50',
    },
    {
      title: 'Published Surveys',
      value: stats?.publishedSurveys ?? 0,
      icon: CheckCircle2,
      color: 'text-emerald-600 bg-emerald-50',
    },
    {
      title: 'Active Public Links',
      value: stats?.activeLinks ?? 0,
      icon: Link2,
      color: 'text-indigo-600 bg-indigo-50',
    },
    {
      title: 'Total Responses',
      value: stats?.totalResponses ?? 0,
      icon: Users,
      color: 'text-purple-600 bg-purple-50',
    },
    {
      title: 'Average Rating',
      value: stats?.averageRating ? `${stats.averageRating} / 5` : 'N/A',
      icon: Star,
      color: 'text-amber-500 bg-amber-50',
    },
  ];

  return (
    <div className="p-6 md:p-10 space-y-8 max-w-7xl mx-auto w-full">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">QMS Survey Dashboard</h1>
          <p className="text-sm text-slate-500 mt-1">
            Real-time healthcare quality monitoring and survey feedback management.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/admin/surveys/new">
            <Button variant="primary" className="gap-2">
              <PlusCircle className="w-4 h-4" />
              Create Survey
            </Button>
          </Link>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
        {kpis.map((kpi, idx) => (
          <Card key={idx} className="relative overflow-hidden">
            <CardContent className="p-5 flex flex-col justify-between h-full">
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-semibold text-slate-500">{kpi.title}</span>
                <div className={`p-2 rounded-xl ${kpi.color}`}>
                  <kpi.icon className="w-4 h-4" />
                </div>
              </div>
              <div className="text-2xl font-bold text-slate-900">
                {isLoading ? <span className="text-slate-300">...</span> : kpi.value}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Recent Submissions */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Recent Patient Submissions</CardTitle>
            <CardDescription>Latest healthcare survey feedback received</CardDescription>
          </div>
          <Link to="/admin/surveys">
            <Button variant="ghost" size="sm" className="gap-1 text-primary-600 hover:text-primary-700">
              View All Surveys <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-8 text-center text-slate-400">Loading recent submissions...</div>
          ) : stats?.recentSubmissions && stats.recentSubmissions.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50/70 text-slate-500 text-xs font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Reference ID</th>
                    <th className="px-6 py-3">Survey Name</th>
                    <th className="px-6 py-3">Respondent</th>
                    <th className="px-6 py-3">Overall Rating</th>
                    <th className="px-6 py-3">Submitted At</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {stats.recentSubmissions.map((sub) => (
                    <tr key={sub.referenceId} className="hover:bg-slate-50/60 transition-colors">
                      <td className="px-6 py-4 font-mono text-xs font-semibold text-primary-700">
                        {sub.referenceId}
                      </td>
                      <td className="px-6 py-4 font-medium text-slate-900">
                        {sub.surveyName}
                      </td>
                      <td className="px-6 py-4 text-slate-600">
                        {sub.respondentName || 'Anonymous'}
                      </td>
                      <td className="px-6 py-4">
                        {sub.overallRating ? (
                          <div className="flex items-center gap-1 text-amber-500 font-semibold text-xs">
                            <Star className="w-3.5 h-3.5 fill-amber-400" />
                            {sub.overallRating} / 5
                          </div>
                        ) : (
                          <span className="text-slate-400 text-xs">-</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-slate-400 text-xs flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5" />
                        {new Date(sub.submittedAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-12 text-center text-slate-500">
              <p className="text-sm font-medium">No survey responses received yet.</p>
              <p className="text-xs text-slate-400 mt-1">Publish a survey and share its public link to collect responses.</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
