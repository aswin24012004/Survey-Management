import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { ProtectedRoute } from './components/layout/ProtectedRoute';
import { AdminLayout } from './components/layout/AdminLayout';
import { AdminLogin } from './pages/auth/AdminLogin';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { SurveyListPage } from './pages/admin/SurveyListPage';
import { CreateOrEditSurveyPage } from './pages/admin/CreateOrEditSurveyPage';
import { SurveyPreviewPage } from './pages/admin/SurveyPreviewPage';
import { SurveyResponsesPage } from './pages/admin/SurveyResponsesPage';
import { SurveyAnalyticsPage } from './pages/admin/SurveyAnalyticsPage';
import { AuditLogsPage } from './pages/admin/AuditLogsPage';
import { PublicSurveyPage } from './pages/public/PublicSurveyPage';
import { NotFoundPage } from './pages/NotFoundPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <BrowserRouter>
            <Routes>
              {/* Public Survey Route - No Auth Required */}
              <Route path="/survey/:token" element={<PublicSurveyPage />} />

              {/* Authentication */}
              <Route path="/login" element={<AdminLogin />} />

              {/* Admin Protected Routes */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute>
                    <AdminLayout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<Navigate to="/admin/dashboard" replace />} />
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="surveys" element={<SurveyListPage />} />
                <Route path="surveys/new" element={<CreateOrEditSurveyPage />} />
                <Route path="surveys/:id/edit" element={<CreateOrEditSurveyPage />} />
                <Route path="surveys/:id/preview" element={<SurveyPreviewPage />} />
                <Route path="surveys/:id/responses" element={<SurveyResponsesPage />} />
                <Route path="surveys/:id/analytics" element={<SurveyAnalyticsPage />} />
                <Route path="audit" element={<AuditLogsPage />} />
              </Route>

              {/* Redirect root to admin or login */}
              <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />

              {/* 404 */}
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </BrowserRouter>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;
