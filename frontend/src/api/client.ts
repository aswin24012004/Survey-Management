import axios, { AxiosError } from 'axios';
import {
  AuthResponse,
  DashboardStats,
  PublicSurvey,
  SurveyAnalytics,
  SurveyLink,
  SurveyResponseItem,
  SurveySummary,
  SurveyTemplate,
  User,
  AuditLog,
  SurveySubmissionRequest,
  SubmissionResponse,
} from '../types';

let accessToken: string | null = null;

export const setAuthToken = (token: string | null) => {
  accessToken = token;
};

export const getAuthToken = () => accessToken;

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  if (accessToken && config.headers) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    if (error.response?.status === 401 && !originalRequest?._retry && !originalRequest?.url?.includes('/auth/login') && !originalRequest?.url?.includes('/auth/refresh')) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const { data } = await axios.post<AuthResponse>(
          `${import.meta.env.VITE_API_BASE_URL || '/api'}/auth/refresh`,
          {},
          { withCredentials: true }
        );
        setAuthToken(data.accessToken);
        processQueue(null, data.accessToken);
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        }
        return api(originalRequest);
      } catch (refreshErr) {
        processQueue(refreshErr, null);
        setAuthToken(null);
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Typed API Functions
export const authApi = {
  login: async (credentials: { username: string; password: string }): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/login', credentials);
    setAuthToken(res.data.accessToken);
    return res.data;
  },
  refresh: async (): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/refresh');
    setAuthToken(res.data.accessToken);
    return res.data;
  },
  logout: async (): Promise<void> => {
    try {
      await api.post('/auth/logout');
    } finally {
      setAuthToken(null);
    }
  },
  getMe: async (): Promise<User> => {
    const res = await api.get<User>('/auth/me');
    return res.data;
  },
};

export const adminApi = {
  getDashboardStats: async (): Promise<DashboardStats> => {
    const res = await api.get<DashboardStats>('/admin/dashboard/stats');
    return res.data;
  },
  getSurveys: async (): Promise<SurveySummary[]> => {
    const res = await api.get<SurveySummary[]>('/admin/surveys');
    return res.data;
  },
  getSurvey: async (id: number): Promise<SurveyTemplate> => {
    const res = await api.get<SurveyTemplate>(`/admin/surveys/${id}`);
    return res.data;
  },
  createSurvey: async (data: any): Promise<SurveyTemplate> => {
    const res = await api.post<SurveyTemplate>('/admin/surveys', data);
    return res.data;
  },
  importSurvey: async (data: any): Promise<SurveyTemplate> => {
    const res = await api.post<SurveyTemplate>('/admin/surveys/import', data);
    return res.data;
  },
  updateSurvey: async (id: number, data: any): Promise<SurveyTemplate> => {
    const res = await api.put<SurveyTemplate>(`/admin/surveys/${id}`, data);
    return res.data;
  },
  publishSurvey: async (id: number, params?: { expiresAt?: string; maxResponses?: number; forceNewLink?: boolean }): Promise<any> => {
    const res = await api.post(`/admin/surveys/${id}/publish`, params || {});
    return res.data;
  },
  regenerateLink: async (id: number, params?: { expiresAt?: string; maxResponses?: number }): Promise<any> => {
    const res = await api.post(`/admin/surveys/${id}/regenerate-link`, params || {});
    return res.data;
  },
  getSurveyLinks: async (id: number): Promise<SurveyLink[]> => {
    const res = await api.get<SurveyLink[]>(`/admin/surveys/${id}/links`);
    return res.data;
  },
  deactivateLink: async (linkId: number): Promise<SurveyLink> => {
    const res = await api.patch<SurveyLink>(`/admin/survey-links/${linkId}/deactivate`);
    return res.data;
  },
  archiveSurvey: async (id: number): Promise<void> => {
    await api.post(`/admin/surveys/${id}/archive`);
  },
  getSurveyResponses: async (id: number): Promise<SurveyResponseItem[]> => {
    const res = await api.get<SurveyResponseItem[]>(`/admin/surveys/${id}/responses`);
    return res.data;
  },
  getSurveyAnalytics: async (id: number): Promise<SurveyAnalytics> => {
    const res = await api.get<SurveyAnalytics>(`/admin/surveys/${id}/analytics`);
    return res.data;
  },
  exportSurveyResponsesUrl: (id: number) => {
    return `${import.meta.env.VITE_API_BASE_URL || '/api'}/admin/surveys/${id}/export`;
  },
  exportSurveyResponses: async (id: number): Promise<Blob> => {
    const res = await api.get(`/admin/surveys/${id}/export`, { responseType: 'blob' });
    return res.data;
  },
  getAuditLogs: async (): Promise<AuditLog[]> => {
    const res = await api.get<AuditLog[]>('/admin/audit-logs');
    return res.data;
  },
};

export const publicApi = {
  getSurvey: async (token: string): Promise<PublicSurvey> => {
    const res = await api.get<PublicSurvey>(`/public/surveys/${token}`);
    return res.data;
  },
  submitResponse: async (token: string, payload: SurveySubmissionRequest): Promise<SubmissionResponse> => {
    const res = await api.post<SubmissionResponse>(`/public/surveys/${token}/responses`, payload);
    return res.data;
  },
};
