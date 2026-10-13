import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { QuestionBuilder } from '../components/survey/QuestionBuilder';
import { QuestionDefinition } from '../types';

describe('QuestionBuilder', () => {
  it('renders existing questions and allows adding a new question', () => {
    const questions: QuestionDefinition[] = [
      {
        id: 'q1',
        text: 'Cleanliness rating',
        type: 'rating',
        required: true,
        min: 1,
        max: 5,
      },
    ];
    const onChange = vi.fn();

    render(<QuestionBuilder questions={questions} onChange={onChange} />);

    expect(screen.getByDisplayValue('Cleanliness rating')).toBeInTheDocument();

    const addBtn = screen.getByRole('button', { name: /add question/i });
    fireEvent.click(addBtn);

    expect(onChange).toHaveBeenCalled();
    const updatedCall = onChange.mock.calls[0][0];
    expect(updatedCall.length).toBe(2);
  });

  it('allows editing question text', () => {
    const questions: QuestionDefinition[] = [
      {
        id: 'q1',
        text: 'Old Question Text',
        type: 'rating',
        required: true,
      },
    ];
    const onChange = vi.fn();

    render(<QuestionBuilder questions={questions} onChange={onChange} />);

    const input = screen.getByDisplayValue('Old Question Text');
    fireEvent.change(input, { target: { value: 'Updated Question Text' } });

    expect(onChange).toHaveBeenCalledWith([
      expect.objectContaining({ text: 'Updated Question Text' }),
    ]);
  });
});
