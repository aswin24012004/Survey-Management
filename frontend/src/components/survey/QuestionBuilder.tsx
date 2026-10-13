import React from 'react';
import { QuestionDefinition, QuestionType } from '../../types';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Plus, Trash2, ArrowUp, ArrowDown, HelpCircle } from 'lucide-react';

interface QuestionBuilderProps {
  questions: QuestionDefinition[];
  onChange: (questions: QuestionDefinition[]) => void;
}

const QUESTION_TYPES: { value: QuestionType; label: string }[] = [
  { value: 'rating', label: 'Rating Scale (1..N)' },
  { value: 'single_choice', label: 'Single Choice (Radio)' },
  { value: 'multiple_choice', label: 'Multiple Choice (Checkboxes)' },
  { value: 'short_text', label: 'Short Text Input' },
  { value: 'long_text', label: 'Long Text (Textarea)' },
  { value: 'dropdown', label: 'Dropdown Selection' },
  { value: 'yes_no', label: 'Yes / No Choice' },
  { value: 'date', label: 'Date Picker' },
  { value: 'number', label: 'Number Input' },
  { value: 'boolean', label: 'Boolean (Agreement)' },
];

export const QuestionBuilder: React.FC<QuestionBuilderProps> = ({ questions, onChange }) => {
  const handleAddQuestion = () => {
    const nextIndex = questions.length + 1;
    const newQuestion: QuestionDefinition = {
      id: `q${Date.now().toString().slice(-4)}`,
      text: '',
      type: 'rating',
      required: true,
      displayOrder: nextIndex,
      min: 1,
      max: 5,
      options: ['Option 1', 'Option 2'],
    };
    onChange([...questions, newQuestion]);
  };

  const handleUpdate = (index: number, partial: Partial<QuestionDefinition>) => {
    const updated = [...questions];
    updated[index] = { ...updated[index], ...partial };
    onChange(updated);
  };

  const handleDelete = (index: number) => {
    const updated = questions.filter((_, i) => i !== index);
    onChange(updated);
  };

  const handleMove = (index: number, direction: 'up' | 'down') => {
    if ((direction === 'up' && index === 0) || (direction === 'down' && index === questions.length - 1)) {
      return;
    }
    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    const updated = [...questions];
    const [moved] = updated.splice(index, 1);
    updated.splice(targetIndex, 0, moved);
    onChange(updated);
  };

  const handleOptionChange = (qIndex: number, optIndex: number, val: string) => {
    const q = questions[qIndex];
    const newOptions = [...(q.options || [])];
    newOptions[optIndex] = val;
    handleUpdate(qIndex, { options: newOptions });
  };

  const handleAddOption = (qIndex: number) => {
    const q = questions[qIndex];
    const newOptions = [...(q.options || []), `Option ${(q.options?.length || 0) + 1}`];
    handleUpdate(qIndex, { options: newOptions });
  };

  const handleRemoveOption = (qIndex: number, optIndex: number) => {
    const q = questions[qIndex];
    const newOptions = (q.options || []).filter((_, i) => i !== optIndex);
    handleUpdate(qIndex, { options: newOptions });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h4 className="text-base font-semibold text-slate-900">Survey Questions ({questions.length})</h4>
          <p className="text-xs text-slate-500">Define the questions to include in this healthcare survey template.</p>
        </div>
        <Button type="button" size="sm" onClick={handleAddQuestion} className="gap-2">
          <Plus className="w-4 h-4" />
          Add Question
        </Button>
      </div>

      {questions.length === 0 ? (
        <div className="text-center py-12 px-4 border-2 border-dashed border-slate-200 rounded-2xl bg-slate-50/50">
          <p className="text-sm font-medium text-slate-600">No questions added yet.</p>
          <p className="text-xs text-slate-400 mt-1">Click "Add Question" above or import questions from JSON.</p>
          <Button type="button" variant="outline" size="sm" onClick={handleAddQuestion} className="mt-4 gap-2">
            <Plus className="w-4 h-4" /> Add First Question
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          {questions.map((q, idx) => (
            <div
              key={q.id || idx}
              className="p-5 bg-white border border-slate-200 rounded-2xl shadow-sm space-y-4 transition-all hover:border-slate-300"
            >
              <div className="flex items-center justify-between gap-3 pb-3 border-b border-slate-100">
                <div className="flex items-center gap-2">
                  <span className="w-6 h-6 rounded-full bg-primary-100 text-primary-700 font-bold text-xs flex items-center justify-center">
                    {idx + 1}
                  </span>
                  <span className="text-xs font-mono text-slate-400">ID: {q.id}</span>
                </div>
                <div className="flex items-center gap-1">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    disabled={idx === 0}
                    onClick={() => handleMove(idx, 'up')}
                    title="Move up"
                    className="p-1.5 h-auto text-slate-500 hover:text-slate-900"
                  >
                    <ArrowUp className="w-4 h-4" />
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    disabled={idx === questions.length - 1}
                    onClick={() => handleMove(idx, 'down')}
                    title="Move down"
                    className="p-1.5 h-auto text-slate-500 hover:text-slate-900"
                  >
                    <ArrowDown className="w-4 h-4" />
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(idx)}
                    title="Delete question"
                    className="p-1.5 h-auto text-rose-500 hover:text-rose-700 hover:bg-rose-50"
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="md:col-span-2">
                  <Input
                    label="Question Text"
                    required
                    placeholder="e.g. How satisfied are you with clinical care?"
                    value={q.text}
                    onChange={(e) => handleUpdate(idx, { text: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Question Type</label>
                  <Select
                    value={q.type}
                    onChange={(e) => handleUpdate(idx, { type: e.target.value as QuestionType })}
                  >
                    {QUESTION_TYPES.map((t) => (
                      <option key={t.value} value={t.value}>
                        {t.label}
                      </option>
                    ))}
                  </Select>
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-6 pt-2">
                <label className="inline-flex items-center gap-2 cursor-pointer text-sm font-medium text-slate-700">
                  <input
                    type="checkbox"
                    checked={q.required}
                    onChange={(e) => handleUpdate(idx, { required: e.target.checked })}
                    className="h-4 w-4 rounded text-primary-600 border-slate-300 focus:ring-primary-500"
                  />
                  Required Field
                </label>

                <div className="flex-1 min-w-[200px]">
                  <Input
                    placeholder="Help text / instructions (optional)"
                    value={q.helpText || ''}
                    onChange={(e) => handleUpdate(idx, { helpText: e.target.value })}
                  />
                </div>
              </div>

              {/* Type-specific configs */}
              {q.type === 'rating' && (
                <div className="flex items-center gap-4 p-3 bg-slate-50 rounded-xl">
                  <span className="text-xs font-medium text-slate-600">Rating Scale Range:</span>
                  <div className="w-24">
                    <Input
                      type="number"
                      placeholder="Min"
                      value={q.min ?? 1}
                      onChange={(e) => handleUpdate(idx, { min: Number(e.target.value) })}
                    />
                  </div>
                  <span className="text-slate-400 text-xs">to</span>
                  <div className="w-24">
                    <Input
                      type="number"
                      placeholder="Max"
                      value={q.max ?? 5}
                      onChange={(e) => handleUpdate(idx, { max: Number(e.target.value) })}
                    />
                  </div>
                </div>
              )}

              {(q.type === 'single_choice' || q.type === 'multiple_choice' || q.type === 'dropdown') && (
                <div className="p-4 bg-slate-50 rounded-xl space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-slate-700">Choice Options:</span>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => handleAddOption(idx)}
                      className="text-xs py-1 h-auto"
                    >
                      <Plus className="w-3 h-3 mr-1" /> Add Option
                    </Button>
                  </div>
                  <div className="space-y-2">
                    {(q.options || []).map((opt, optIdx) => (
                      <div key={optIdx} className="flex items-center gap-2">
                        <Input
                          value={opt}
                          placeholder={`Option ${optIdx + 1}`}
                          onChange={(e) => handleOptionChange(idx, optIdx, e.target.value)}
                        />
                        {(q.options || []).length > 2 && (
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={() => handleRemoveOption(idx, optIdx)}
                            className="p-1.5 text-slate-400 hover:text-rose-600"
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
