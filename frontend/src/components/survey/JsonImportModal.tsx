import React, { useState } from 'react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { useToast } from '../../context/ToastContext';
import { adminApi } from '../../api/client';
import { FileJson, Sparkles } from 'lucide-react';

interface JsonImportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onImportSuccess: () => void;
}

const SAMPLE_TEMPLATE_JSON = JSON.stringify(
  {
    template: {
      name: "Hospital Patient Satisfaction Survey",
      description: "Patient experience feedback to improve clinical care quality",
      category: "PATIENT_CARE",
      questions: [
        {
          id: "q1",
          text: "How satisfied are you with the waiting time before your consultation?",
          type: "rating",
          min: 1,
          max: 5,
          required: true,
          helpText: "1 = Very Dissatisfied, 5 = Very Satisfied"
        },
        {
          id: "q2",
          text: "Did the doctor explain your medical condition and treatment clearly?",
          type: "single_choice",
          options: ["Yes", "No", "Partially"],
          required: true
        },
        {
          id: "q3",
          text: "How would you rate the cleanliness of the hospital and patient rooms?",
          type: "rating",
          min: 1,
          max: 5,
          required: true
        },
        {
          id: "q4",
          text: "Would you recommend our healthcare facility to family and friends?",
          type: "yes_no",
          required: true
        }
      ]
    }
  },
  null,
  2
);

export const JsonImportModal: React.FC<JsonImportModalProps> = ({
  isOpen,
  onClose,
  onImportSuccess,
}) => {
  const { showToast } = useToast();
  const [jsonText, setJsonText] = useState(SAMPLE_TEMPLATE_JSON);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleImport = async () => {
    setError(null);
    let parsed: any;
    try {
      parsed = JSON.parse(jsonText);
    } catch (e: any) {
      setError(`Invalid JSON syntax: ${e.message}`);
      return;
    }

    setIsLoading(true);
    try {
      await adminApi.importSurvey(parsed);
      showToast('Survey template imported successfully!', 'success');
      onImportSuccess();
      onClose();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to import survey template');
    } finally {
      setIsLoading(false);
    }
  };

  const loadSample = () => {
    setJsonText(SAMPLE_TEMPLATE_JSON);
    setError(null);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Import Survey Template (JSON)"
      description="Upload or paste a standard Healthcare Survey template definition."
      maxWidth="2xl"
    >
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <span className="text-xs text-slate-500 flex items-center gap-1">
            <FileJson className="w-3.5 h-3.5" /> Supports Jackson dynamic format
          </span>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={loadSample}
            className="text-xs gap-1 text-primary-600 hover:text-primary-700"
          >
            <Sparkles className="w-3.5 h-3.5" /> Load Sample JSON
          </Button>
        </div>

        {error && (
          <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl font-medium">
            {error}
          </div>
        )}

        <textarea
          rows={14}
          value={jsonText}
          onChange={(e) => setJsonText(e.target.value)}
          className="w-full font-mono text-xs p-4 bg-slate-900 text-emerald-400 rounded-xl border border-slate-700 focus:outline-none focus:ring-2 focus:ring-primary-500 resize-y"
        />

        <div className="flex justify-end gap-3 pt-2">
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleImport} isLoading={isLoading}>
            Import Template
          </Button>
        </div>
      </div>
    </Modal>
  );
};
