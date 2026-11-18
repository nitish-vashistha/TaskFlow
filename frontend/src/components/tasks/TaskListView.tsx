import React from 'react';
import { Task, TaskStatus } from '../../types';
import { Badge } from '../common/Badge';

interface TaskListViewProps {
  tasks: Task[];
  onEditTask: (task: Task) => void;
  onDeleteTask: (taskId: number) => void;
  onStatusChange: (taskId: number, newStatus: TaskStatus) => void;
}

export const TaskListView: React.FC<TaskListViewProps> = ({
  tasks,
  onEditTask,
  onDeleteTask,
  onStatusChange,
}) => {
  if (tasks.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-12 text-center text-sm text-gray-500">
        No tasks match the selected filters.
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-xs">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 text-left text-sm">
          <thead className="bg-gray-50/80 text-xs font-semibold uppercase tracking-wider text-gray-500">
            <tr>
              <th className="px-6 py-3.5">Title</th>
              <th className="px-4 py-3.5">Status</th>
              <th className="px-4 py-3.5">Priority</th>
              <th className="px-4 py-3.5">Due Date</th>
              <th className="px-4 py-3.5">Assignee</th>
              <th className="px-6 py-3.5 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 bg-white">
            {tasks.map((task) => (
              <tr key={task.id} className="hover:bg-gray-50/70 transition-colors">
                <td className="px-6 py-4 font-medium text-gray-900 max-w-xs truncate">
                  <div className="flex flex-col">
                    <span className="truncate">{task.title}</span>
                    {task.description && (
                      <span className="text-xs text-gray-400 truncate mt-0.5">{task.description}</span>
                    )}
                  </div>
                </td>
                <td className="px-4 py-4 whitespace-nowrap">
                  <select
                    value={task.status}
                    onChange={(e) => onStatusChange(task.id, e.target.value as TaskStatus)}
                    className="text-xs font-medium bg-transparent border border-gray-200 rounded-md px-2 py-1 text-gray-700 hover:border-gray-300 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  >
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="DONE">Done</option>
                  </select>
                </td>
                <td className="px-4 py-4 whitespace-nowrap">
                  <Badge variant="priority" priority={task.priority} size="sm" />
                </td>
                <td className="px-4 py-4 whitespace-nowrap text-xs">
                  <div className="flex items-center space-x-1.5">
                    <span className={task.isOverdue ? 'text-red-600 font-semibold' : 'text-gray-600'}>
                      {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '—'}
                    </span>
                    {task.isOverdue && (
                      <Badge variant="danger" size="sm">Overdue</Badge>
                    )}
                  </div>
                </td>
                <td className="px-4 py-4 whitespace-nowrap text-xs text-gray-600">
                  {task.assigneeName ? (
                    <div className="flex items-center space-x-1.5">
                      <span className="w-5 h-5 rounded-full bg-blue-100 text-blue-700 font-semibold text-[10px] flex items-center justify-center">
                        {task.assigneeName.charAt(0).toUpperCase()}
                      </span>
                      <span>{task.assigneeName}</span>
                    </div>
                  ) : (
                    <span className="text-gray-400 italic">Unassigned</span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-xs font-medium space-x-3">
                  <button
                    onClick={() => onEditTask(task)}
                    className="text-blue-600 hover:text-blue-900"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => {
                      if (window.confirm(`Delete task "${task.title}"?`)) {
                        onDeleteTask(task.id);
                      }
                    }}
                    className="text-red-600 hover:text-red-900"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
