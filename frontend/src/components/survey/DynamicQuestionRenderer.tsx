import React from 'react';
import { QuestionDefinition } from '../../types';
import { Star, Check } from 'lucide-react';

interface DynamicQuestionRendererProps {
  question: QuestionDefinition;
  value: any;
  onChange: (value: any) => void;
  error?: string;
  disabled?: boolean;
}

export const DynamicQuestionRenderer: React.FC<DynamicQuestionRendererProps> = ({
  question,
  value,
  onChange,
  error,
  disabled = false,
}) => {
  const { id, text, type, required, helpText, placeholder, min = 1, max = 5, options = [] } = question;

  const renderInput = () => {
    switch (type) {
      case 'rating': {
        const minVal = min ?? 1;
        const maxVal = max ?? 5;
        const count = Math.max(1, maxVal - minVal + 1);
        const numbers = Array.from({ length: count }, (_, i) => minVal + i);

        // If standard 5-point rating, render attractive star rating with numeric badges
        return (
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              {numbers.map((num) => {
                const isSelected = value === num;
                return (
                  <button
                    key={num}
                    type="button"
                    disabled={disabled}
                    onClick={() => onChange(num)}
                    className={`flex items-center justify-center w-12 h-12 rounded-xl text-base font-semibold transition-all border ${
                      isSelected
                        ? 'bg-primary-600 text-white border-primary-600 shadow-md scale-105 ring-2 ring-primary-300'
                        : 'bg-white text-slate-700 border-slate-200 hover:border-primary-400 hover:bg-primary-50'
                    }`}
                  >
                    <span className="flex items-center gap-1">
                      {num}
                      <Star className={`w-3.5 h-3.5 ${isSelected ? 'fill-white text-white' : 'text-amber-400'}`} />
                    </span>
                  </button>
                );
              })}
            </div>
            <div className="flex justify-between text-xs text-slate-400 max-w-sm">
              <span>{minVal} = Lowest</span>
              <span>{maxVal} = Highest</span>
            </div>
          </div>
        );
      }

      case 'single_choice': {
        return (
          <div className="space-y-2">
            {options.map((option, idx) => {
              const isSelected = value === option;
              return (
                <label
                  key={idx}
                  className={`flex items-center p-3 rounded-xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'border-primary-500 bg-primary-50/60 ring-1 ring-primary-400 text-primary-950 font-medium'
                      : 'border-slate-200 bg-white hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="radio"
                    name={id}
                    value={option}
                    checked={isSelected}
                    disabled={disabled}
                    onChange={() => onChange(option)}
                    className="h-4 w-4 text-primary-600 border-slate-300 focus:ring-primary-500"
                  />
                  <span className="ml-3 text-sm">{option}</span>
                </label>
              );
            })}
          </div>
        );
      }

      case 'multiple_choice': {
        const selectedList: string[] = Array.isArray(value) ? value : [];
        const handleToggle = (option: string) => {
          if (selectedList.includes(option)) {
            onChange(selectedList.filter((item) => item !== option));
          } else {
            onChange([...selectedList, option]);
          }
        };

        return (
          <div className="space-y-2">
            {options.map((option, idx) => {
              const isSelected = selectedList.includes(option);
              return (
                <label
                  key={idx}
                  className={`flex items-center p-3 rounded-xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'border-primary-500 bg-primary-50/60 ring-1 ring-primary-400 text-primary-950 font-medium'
                      : 'border-slate-200 bg-white hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={isSelected}
                    disabled={disabled}
                    onChange={() => handleToggle(option)}
                    className="h-4 w-4 rounded text-primary-600 border-slate-300 focus:ring-primary-500"
                  />
                  <span className="ml-3 text-sm">{option}</span>
                </label>
              );
            })}
          </div>
        );
      }

      case 'short_text': {
        return (
          <input
            type="text"
            value={value || ''}
            disabled={disabled}
            placeholder={placeholder || 'Enter your response...'}
            onChange={(e) => onChange(e.target.value)}
            className="w-full px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-xl transition-all focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-transparent placeholder:text-slate-400"
          />
        );
      }

      case 'long_text': {
        return (
          <textarea
            rows={4}
            value={value || ''}
            disabled={disabled}
            placeholder={placeholder || 'Provide detailed feedback here...'}
            onChange={(e) => onChange(e.target.value)}
            className="w-full px-4 py-3 text-sm bg-white border border-slate-200 rounded-xl transition-all focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-transparent placeholder:text-slate-400 resize-y"
          />
        );
      }

      case 'dropdown': {
        return (
          <select
            value={value || ''}
            disabled={disabled}
            onChange={(e) => onChange(e.target.value)}
            className="w-full px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-xl transition-all focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-transparent"
          >
            <option value="" disabled>
              {placeholder || '-- Select an option --'}
            </option>
            {options.map((opt, idx) => (
              <option key={idx} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        );
      }

      case 'yes_no': {
        return (
          <div className="flex items-center gap-3">
            {['Yes', 'No'].map((opt) => {
              const isSelected = value?.toString().toLowerCase() === opt.toLowerCase();
              return (
                <button
                  key={opt}
                  type="button"
                  disabled={disabled}
                  onClick={() => onChange(opt)}
                  className={`flex-1 py-3 px-5 rounded-xl text-sm font-semibold border transition-all flex items-center justify-center gap-2 ${
                    isSelected
                      ? opt === 'Yes'
                        ? 'bg-emerald-600 text-white border-emerald-600 shadow-md ring-2 ring-emerald-300'
                        : 'bg-rose-600 text-white border-rose-600 shadow-md ring-2 ring-rose-300'
                      : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
                  }`}
                >
                  {isSelected && <Check className="w-4 h-4 stroke-[3]" />}
                  {opt}
                </button>
              );
            })}
          </div>
        );
      }

      case 'date': {
        return (
          <input
            type="date"
            value={value || ''}
            disabled={disabled}
            onChange={(e) => onChange(e.target.value)}
            className="w-full sm:w-64 px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-xl transition-all focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-transparent"
          />
        );
      }

      case 'number': {
        return (
          <input
            type="number"
            value={value !== undefined ? value : ''}
            disabled={disabled}
            placeholder={placeholder || 'Enter a number...'}
            min={min}
            max={max}
            onChange={(e) => {
              const v = e.target.value;
              onChange(v === '' ? '' : Number(v));
            }}
            className="w-full sm:w-64 px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-xl transition-all focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-transparent"
          />
        );
      }

      case 'boolean': {
        const isChecked = !!value;
        return (
          <label className="inline-flex items-center gap-3 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={isChecked}
              disabled={disabled}
              onChange={(e) => onChange(e.target.checked)}
              className="h-5 w-5 rounded text-primary-600 border-slate-300 focus:ring-primary-500"
            />
            <span className="text-sm font-medium text-slate-700">
              {placeholder || 'I confirm / agree'}
            </span>
          </label>
        );
      }

      default:
        return (
          <p className="text-xs text-rose-500">
            Unsupported question type: {(question as any).type}
          </p>
        );
    }
  };

  return (
    <div className="p-5 sm:p-6 bg-white rounded-2xl border border-slate-200/90 shadow-sm transition-all hover:border-slate-300">
      <div className="mb-3">
        <label className="block text-base font-semibold text-slate-900 leading-snug">
          {text}
          {required && <span className="text-rose-500 ml-1 font-bold">*</span>}
        </label>
        {helpText && <p className="text-xs text-slate-500 mt-1">{helpText}</p>}
      </div>

      <div className="mt-4">{renderInput()}</div>

      {error && (
        <p className="mt-2 text-xs font-medium text-rose-600 animate-in fade-in">
          {error}
        </p>
      )}
    </div>
  );
};
