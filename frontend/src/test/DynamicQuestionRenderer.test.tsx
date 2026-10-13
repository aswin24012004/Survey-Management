import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { DynamicQuestionRenderer } from '../components/survey/DynamicQuestionRenderer';
import { QuestionDefinition } from '../types';

describe('DynamicQuestionRenderer', () => {
  it('renders rating question and handles rating selection', () => {
    const question: QuestionDefinition = {
      id: 'q1',
      text: 'How satisfied are you with the waiting time?',
      type: 'rating',
      required: true,
      min: 1,
      max: 5,
    };
    const onChange = vi.fn();

    render(
      <DynamicQuestionRenderer
        question={question}
        value={null}
        onChange={onChange}
      />
    );

    expect(screen.getByText('How satisfied are you with the waiting time?')).toBeInTheDocument();
    expect(screen.getByText('*')).toBeInTheDocument();

    const starBtn4 = screen.getByRole('button', { name: /4/i });
    fireEvent.click(starBtn4);
    expect(onChange).toHaveBeenCalledWith(4);
  });

  it('renders single choice options and triggers selection', () => {
    const question: QuestionDefinition = {
      id: 'q2',
      text: 'Did the doctor explain your medical condition clearly?',
      type: 'single_choice',
      required: true,
      options: ['Yes', 'No', 'Partially'],
    };
    const onChange = vi.fn();

    render(
      <DynamicQuestionRenderer
        question={question}
        value="Yes"
        onChange={onChange}
      />
    );

    expect(screen.getByText('Yes')).toBeInTheDocument();
    expect(screen.getByText('No')).toBeInTheDocument();
    expect(screen.getByText('Partially')).toBeInTheDocument();

    const radioNo = screen.getByLabelText('No');
    fireEvent.click(radioNo);
    expect(onChange).toHaveBeenCalledWith('No');
  });

  it('renders multiple choice checkboxes and handles multi-select', () => {
    const question: QuestionDefinition = {
      id: 'q3',
      text: 'Select symptoms experienced',
      type: 'multiple_choice',
      required: false,
      options: ['Fever', 'Cough', 'Fatigue'],
    };
    const onChange = vi.fn();

    render(
      <DynamicQuestionRenderer
        question={question}
        value={['Fever']}
        onChange={onChange}
      />
    );

    const coughCheckbox = screen.getByLabelText('Cough');
    fireEvent.click(coughCheckbox);
    expect(onChange).toHaveBeenCalledWith(['Fever', 'Cough']);
  });

  it('renders text inputs and displays validation error message', () => {
    const question: QuestionDefinition = {
      id: 'q4',
      text: 'Comments and suggestions',
      type: 'long_text',
      required: true,
      placeholder: 'Enter feedback...',
    };
    const onChange = vi.fn();

    render(
      <DynamicQuestionRenderer
        question={question}
        value=""
        onChange={onChange}
        error="This question is required. Please provide an answer."
      />
    );

    expect(screen.getByPlaceholderText('Enter feedback...')).toBeInTheDocument();
    expect(
      screen.getByText('This question is required. Please provide an answer.')
    ).toBeInTheDocument();
  });

  it('renders Yes/No buttons and selects Yes or No', () => {
    const question: QuestionDefinition = {
      id: 'q5',
      text: 'Would you recommend this clinic?',
      type: 'yes_no',
      required: true,
    };
    const onChange = vi.fn();

    render(
      <DynamicQuestionRenderer
        question={question}
        value=""
        onChange={onChange}
      />
    );

    const yesBtn = screen.getByRole('button', { name: /yes/i });
    fireEvent.click(yesBtn);
    expect(onChange).toHaveBeenCalledWith('Yes');
  });
});
