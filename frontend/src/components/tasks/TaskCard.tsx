import React from 'react';
import { Task, TaskStatus } from '../../types';
import { Badge } from '../common/Badge';

interface TaskCardProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (taskId: number) => void;
  onStatusChange: (taskId: number, newStatus: TaskStatus) => void;
}

export const TaskCard: React.FC<TaskCardProps> = ({
  task,
  onEdit,
  onDelete,
  onStatusChange,
}) => {
  const handleDragStart = (e: React.DragEvent) => {
    e.dataTransfer.setData('text/plain', String(task.id));
    e.dataTransfer.effectAllowed = 'move';
  };

  return (
    <div
      draggable
      onDragStart={handleDragStart}
      className="bg-white rounded-xl border border-gray-200/90 shadow-xs hover:shadow-md transition-all duration-150 p-4 space-y-3 group cursor-grab active:cursor-grabbing"
    >
      {/* Header & Badges */}
      <div className="flex items-center justify-between gap-2">
        <Badge variant="priority" priority={task.priority} size="sm" />
        {task.isOverdue && (
          <Badge variant="danger" size="sm">
            Overdue
          </Badge>
        )}
        <div className="flex items-center space-x-1 opacity-0 group-hover:opacity-100 transition-opacity ml-auto">
          <button
            onClick={() => onEdit(task)}
            className="p-1 text-gray-400 hover:text-blue-600 rounded-md hover:bg-gray-100"
            title="Edit task"
          >
            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
            </svg>
          </button>
          <button
            onClick={() => {
              if (window.confirm(`Delete task "${task.title}"?`)) {
                onDelete(task.id);
              }
            }}
            className="p-1 text-gray-400 hover:text-red-600 rounded-md hover:bg-gray-100"
            title="Delete task"
          >
            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>

      {/* Title & Description */}
      <div>
        <h4 className="text-sm font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
          {task.title}
        </h4>
        {task.description && (
          <p className="mt-1 text-xs text-gray-500 line-clamp-2">
            {task.description}
          </p>
        )}
      </div>

      {/* Due date & Assignee */}
      <div className="pt-2 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
        <div className="flex items-center space-x-1.5">
          <svg className="w-3.5 h-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span className={task.isOverdue ? 'text-red-600 font-medium' : ''}>
            {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No date'}
          </span>
        </div>

        {task.assigneeName ? (
          <div className="flex items-center space-x-1" title={`Assigned to ${task.assigneeName}`}>
            <span className="w-5 h-5 rounded-full bg-blue-100 text-blue-700 font-semibold text-[10px] flex items-center justify-center">
              {task.assigneeName.charAt(0).toUpperCase()}
            </span>
            <span className="text-gray-700 font-medium truncate max-w-[80px]">
              {task.assigneeName.split(' ')[0]}
            </span>
          </div>
        ) : (
          <span className="text-gray-400 text-[11px] italic">Unassigned</span>
        )}
      </div>

      {/* Quick Move Status Buttons */}
      <div className="pt-2 flex items-center justify-between gap-1 text-[11px]">
        {task.status !== 'TODO' && (
          <button
            onClick={() => onStatusChange(task.id, task.status === 'DONE' ? 'IN_PROGRESS' : 'TODO')}
            className="text-gray-500 hover:text-gray-800 hover:bg-gray-100 px-2 py-0.5 rounded transition-colors flex items-center"
          >
            &larr; {task.status === 'DONE' ? 'In Progress' : 'To Do'}
          </button>
        )}
        <div className="flex-1" />
        {task.status !== 'DONE' && (
          <button
            onClick={() => onStatusChange(task.id, task.status === 'TODO' ? 'IN_PROGRESS' : 'DONE')}
            className="text-blue-600 font-medium hover:text-blue-700 hover:bg-blue-50 px-2 py-0.5 rounded transition-colors flex items-center ml-auto"
          >
            {task.status === 'TODO' ? 'In Progress' : 'Done'} &rarr;
          </button>
        )}
      </div>
    </div>
  );
};
