import React from 'react';
import { TaskPriority, TaskStatus } from '../../types';

interface BadgeProps {
  children?: React.ReactNode;
  variant?: 'priority' | 'status' | 'default' | 'danger';
  priority?: TaskPriority;
  status?: TaskStatus;
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'default',
  priority,
  status,
  size = 'md',
}) => {
  const sizeClasses = size === 'sm' ? 'px-2 py-0.5 text-[11px]' : 'px-2.5 py-1 text-xs';

  if (variant === 'priority' && priority) {
    const priorityStyles = {
      LOW: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      MEDIUM: 'bg-amber-50 text-amber-700 border-amber-200',
      HIGH: 'bg-rose-50 text-rose-700 border-rose-200',
    };
    return (
      <span className={`inline-flex items-center font-medium rounded-md border ${sizeClasses} ${priorityStyles[priority]}`}>
        {priority}
      </span>
    );
  }

  if (variant === 'status' && status) {
    const statusStyles = {
      TODO: 'bg-gray-100 text-gray-700 border-gray-200',
      IN_PROGRESS: 'bg-blue-50 text-blue-700 border-blue-200',
      DONE: 'bg-green-50 text-green-700 border-green-200',
    };
    const labels = {
      TODO: 'To Do',
      IN_PROGRESS: 'In Progress',
      DONE: 'Done',
    };
    return (
      <span className={`inline-flex items-center font-medium rounded-md border ${sizeClasses} ${statusStyles[status]}`}>
        {labels[status]}
      </span>
    );
  }

  if (variant === 'danger') {
    return (
      <span className={`inline-flex items-center font-medium rounded-md border bg-red-50 text-red-700 border-red-200 ${sizeClasses}`}>
        {children}
      </span>
    );
  }

  return (
    <span className={`inline-flex items-center font-medium rounded-md border bg-gray-50 text-gray-700 border-gray-200 ${sizeClasses}`}>
      {children}
    </span>
  );
};
