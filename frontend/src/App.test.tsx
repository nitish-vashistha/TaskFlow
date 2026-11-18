import { render, screen } from '@testing-library/react';
import App from './App';

describe('App Router', () => {
  it('redirects unauthenticated users to login page', () => {
    render(<App />);
    expect(screen.getByText('Sign in to TaskFlow')).toBeInTheDocument();
    expect(screen.getByText('Work Email')).toBeInTheDocument();
  });
});
