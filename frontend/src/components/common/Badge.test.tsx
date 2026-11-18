import { render, screen } from '@testing-library/react';
import { Badge } from './Badge';

describe('Badge Component', () => {
  it('renders priority badges with correct styles', () => {
    const { rerender } = render(<Badge variant="priority" priority="HIGH" />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();

    rerender(<Badge variant="priority" priority="LOW" />);
    expect(screen.getByText('LOW')).toBeInTheDocument();
  });

  it('renders status badges with readable labels', () => {
    const { rerender } = render(<Badge variant="status" status="TODO" />);
    expect(screen.getByText('To Do')).toBeInTheDocument();

    rerender(<Badge variant="status" status="IN_PROGRESS" />);
    expect(screen.getByText('In Progress')).toBeInTheDocument();

    rerender(<Badge variant="status" status="DONE" />);
    expect(screen.getByText('Done')).toBeInTheDocument();
  });
});
