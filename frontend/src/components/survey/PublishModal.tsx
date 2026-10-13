import React, { useState } from 'react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { useToast } from '../../context/ToastContext';
import { adminApi } from '../../api/client';
import { Copy, Check, RefreshCw, PowerOff, ExternalLink, Calendar, Users } from 'lucide-react';

interface PublishModalProps {
  isOpen: boolean;
  onClose: () => void;
  surveyId: number;
  surveyName: string;
  initialLinkUrl?: string;
  initialToken?: string;
  initialIsActive?: boolean;
  onPublished?: () => void;
}

export const PublishModal: React.FC<PublishModalProps> = ({
  isOpen,
  onClose,
  surveyId,
  surveyName,
  initialLinkUrl,
  initialToken,
  initialIsActive = true,
  onPublished,
}) => {
  const { showToast } = useToast();
  const [surveyUrl, setSurveyUrl] = useState<string>(initialLinkUrl || '');
  const [linkToken, setLinkToken] = useState<string>(initialToken || '');
  const [isActive, setIsActive] = useState<boolean>(initialIsActive);
  const [copied, setCopied] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [expiresAt, setExpiresAt] = useState('');
  const [maxResponses, setMaxResponses] = useState('');

  // Handle Publish / Get Link
  const handlePublish = async (forceNew = false) => {
    setIsLoading(true);
    try {
      const payload: any = {
        forceNewLink: forceNew,
      };
      if (expiresAt) {
        payload.expiresAt = new Date(expiresAt).toISOString();
      }
      if (maxResponses && Number(maxResponses) > 0) {
        payload.maxResponses = Number(maxResponses);
      }

      const res = await adminApi.publishSurvey(surveyId, payload);
      setSurveyUrl(res.surveyUrl);
      setLinkToken(res.publicToken);
      setIsActive(res.active);
      showToast(forceNew ? 'New survey link regenerated!' : 'Survey published successfully!', 'success');
      onPublished?.();
    } catch (err: any) {
      showToast(err?.response?.data?.message || 'Failed to publish survey', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopy = () => {
    if (!surveyUrl) return;
    navigator.clipboard.writeText(surveyUrl);
    setCopied(true);
    showToast('Survey link copied to clipboard!', 'success');
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Publish & Share Survey Link"
      description={`Generate and distribute public link for "${surveyName}"`}
      maxWidth="lg"
    >
      <div className="space-y-6">
        {surveyUrl ? (
          <div className="space-y-4">
            <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
                <span className="text-xs font-semibold text-emerald-800">
                  {isActive ? 'Public Survey Link Active' : 'Survey Link Deactivated'}
                </span>
              </div>
              <span className="text-xs text-emerald-700 font-mono">Multiple Submissions Enabled</span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Shareable Public URL (No Login Required)
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  readOnly
                  value={surveyUrl}
                  className="flex-1 px-3 py-2 text-xs font-mono bg-slate-50 border border-slate-300 rounded-xl text-slate-800 focus:outline-none"
                />
                <Button variant="primary" size="md" onClick={handleCopy} className="gap-2">
                  {copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
                  {copied ? 'Copied' : 'Copy Link'}
                </Button>
              </div>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <a href={surveyUrl} target="_blank" rel="noopener noreferrer" className="flex-1">
                <Button variant="outline" size="sm" className="w-full gap-2">
                  <ExternalLink className="w-4 h-4" /> Test Public Link
                </Button>
              </a>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePublish(true)}
                isLoading={isLoading}
                className="gap-2 text-slate-700 hover:text-slate-900"
              >
                <RefreshCw className="w-4 h-4" /> Regenerate Link
              </Button>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <p className="text-sm text-slate-600">
              Publishing creates an immutable version snapshot of the questions and generates a cryptographically unpredictable link that can be shared with anyone.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 p-4 bg-slate-50 rounded-2xl border border-slate-200">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1 flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-slate-500" /> Link Expiry (Optional)
                </label>
                <Input
                  type="datetime-local"
                  value={expiresAt}
                  onChange={(e) => setExpiresAt(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1 flex items-center gap-1.5">
                  <Users className="w-3.5 h-3.5 text-slate-500" /> Max Responses (Optional)
                </label>
                <Input
                  type="number"
                  placeholder="Unlimited"
                  value={maxResponses}
                  onChange={(e) => setMaxResponses(e.target.value)}
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <Button variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button variant="primary" onClick={() => handlePublish(false)} isLoading={isLoading}>
                Publish & Generate Link
              </Button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
};
