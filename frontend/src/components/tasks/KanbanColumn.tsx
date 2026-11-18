import React from 'react';
import { Task, TaskStatus } from '../../types';
import { TaskCard } from './TaskCard';

interface KanbanColumnProps {
  status: TaskStatus;
  title: string;
  tasks: Task[];
  onEditTask: (task: Task) => void;
  onDeleteTask: (taskId: number) => void;
  onStatusChange: (taskId: number, newStatus: TaskStatus) => void;
}

export const KanbanColumn: React.FC<KanbanColumnProps> = ({
  status,
  title,
  tasks,
  onEditTask,
  onDeleteTask,
  onStatusChange,
}) => {
  const [isDragOver, setIsDragOver] = React.useState(false);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    if (!isDragOver) setIsDragOver(true);
  };

  const handleDragLeave = () => {
    setIsDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    const taskIdStr = e.dataTransfer.getData('text/plain');
    if (taskIdStr) {
      const taskId = Number(taskIdStr);
      if (!isNaN(taskId)) {
        onStatusChange(taskId, status);
      }
    }
  };

  const columnStyles = {
    TODO: {
      indicator: 'bg-gray-400',
      badge: 'bg-gray-100 text-gray-700',
    },
    IN_PROGRESS: {
      indicator: 'bg-blue-500',
      badge: 'bg-blue-100 text-blue-700',
    },
    DONE: {
      indicator: 'bg-emerald-500',
      badge: 'bg-emerald-100 text-emerald-700',
    },
  };

  return (
    <div
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      className={`flex flex-col rounded-2xl p-4 border transition-all duration-150 min-h-[500px] ${
        isDragOver
          ? 'bg-blue-50/70 border-blue-300 ring-2 ring-blue-400/20'
          : 'bg-gray-100/70 border-gray-200/80'
      }`}
    >
      {/* Column Header */}
      <div className="flex items-center justify-between mb-4 px-1">
        <div className="flex items-center space-x-2">
          <div className={`w-2.5 h-2.5 rounded-full ${columnStyles[status].indicator}`} />
          <h3 className="font-semibold text-sm text-gray-800">{title}</h3>
        </div>
        <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${columnStyles[status].badge}`}>
          {tasks.length}
        </span>
      </div>

      {/* Task List */}
      <div className="flex-1 space-y-3 overflow-y-auto">
        {tasks.length === 0 ? (
          <div className="h-32 border-2 border-dashed border-gray-200 rounded-xl flex items-center justify-center text-xs text-gray-400">
            No tasks in {title.toLowerCase()}
          </div>
        ) : (
          tasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              onEdit={onEditTask}
              onDelete={onDeleteTask}
              onStatusChange={onStatusChange}
            />
          ))
        )}
      </div>
    </div>
  );
};
