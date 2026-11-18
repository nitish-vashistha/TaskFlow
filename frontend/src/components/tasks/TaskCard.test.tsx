import { render, screen, fireEvent } from '@testing-library/react';
import { TaskCard } from './TaskCard';
import { Task } from '../../types';

describe('TaskCard', () => {
  const mockTask: Task = {
    id: 1,
    title: 'Design high fidelity UI',
    description: 'Create Figma components for Kanban board',
    status: 'TODO',
    priority: 'HIGH',
    dueDate: '2026-12-01',
    isOverdue: false,
    projectId: 10,
    projectName: 'TaskFlow',
    assigneeId: 2,
    assigneeName: 'Jane Doe',
    assigneeEmail: 'jane@example.com',
    createdAt: '2026-11-18T10:00:00Z',
    updatedAt: '2026-11-18T10:00:00Z',
  };

  const mockEdit = jest.fn();
  const mockDelete = jest.fn();
  const mockStatusChange = jest.fn();

  it('renders task title, priority, and assignee', () => {
    render(
      <TaskCard
        task={mockTask}
        onEdit={mockEdit}
        onDelete={mockDelete}
        onStatusChange={mockStatusChange}
      />
    );

    expect(screen.getByText('Design high fidelity UI')).toBeInTheDocument();
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('Jane')).toBeInTheDocument();
  });

  it('displays overdue badge when task is overdue', () => {
    const overdueTask: Task = { ...mockTask, isOverdue: true };
    render(
      <TaskCard
        task={overdueTask}
        onEdit={mockEdit}
        onDelete={mockDelete}
        onStatusChange={mockStatusChange}
      />
    );

    expect(screen.getByText('Overdue')).toBeInTheDocument();
  });

  it('calls onStatusChange when quick move button clicked', () => {
    render(
      <TaskCard
        task={mockTask}
        onEdit={mockEdit}
        onDelete={mockDelete}
        onStatusChange={mockStatusChange}
      />
    );

    const moveButton = screen.getByText(/In Progress/);
    fireEvent.click(moveButton);
    expect(mockStatusChange).toHaveBeenCalledWith(1, 'IN_PROGRESS');
  });
});
