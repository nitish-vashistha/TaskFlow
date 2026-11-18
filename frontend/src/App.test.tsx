import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

describe('App Component', () => {
  it('renders TaskFlow headline', () => {
    render(<App />);
    expect(screen.getByText('TaskFlow')).toBeInTheDocument();
    expect(screen.getByText('Team Task Management System')).toBeInTheDocument();
  });
});
