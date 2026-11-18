import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { projectsApi } from '../api/projects';
import { tasksApi } from '../api/tasks';
import { Task, TaskStatus } from '../types';
import { KanbanBoard } from '../components/tasks/KanbanBoard';
import { TaskListView } from '../components/tasks/TaskListView';
import { TaskModal } from '../components/tasks/TaskModal';
import { Button } from '../components/common/Button';
import { Spinner } from '../components/common/Spinner';

export const ProjectPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);

  const queryClient = useQueryClient();

  const [viewMode, setViewMode] = useState<'kanban' | 'list'>('kanban');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  // Queries
  const { data: project, isLoading: isProjectLoading, error: projectError } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectsApi.getProjectById(projectId),
    enabled: !!projectId,
  });

  const { data: summary } = useQuery({
    queryKey: ['project-summary', projectId],
    queryFn: () => projectsApi.getProjectSummary(projectId),
    enabled: !!projectId,
  });

  const { data: tasks = [], isLoading: isTasksLoading } = useQuery({
    queryKey: ['kanban-tasks', projectId],
    queryFn: () => tasksApi.getKanbanTasks(projectId),
    enabled: !!projectId,
  });

  // Mutations
  const createTaskMutation = useMutation({
    mutationFn: (data: any) => tasksApi.createTask(projectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['kanban-tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      setIsModalOpen(false);
    },
  });

  const updateTaskMutation = useMutation({
    mutationFn: ({ taskId, data }: { taskId: number; data: any }) =>
      tasksApi.updateTask(projectId, taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['kanban-tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      setIsModalOpen(false);
      setEditingTask(null);
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ taskId, status }: { taskId: number; status: TaskStatus }) =>
      tasksApi.updateTaskStatus(projectId, taskId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['kanban-tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
    },
  });

  const deleteTaskMutation = useMutation({
    mutationFn: (taskId: number) => tasksApi.deleteTask(projectId, taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['kanban-tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
    },
  });

  // Filter tasks locally for responsive instant filtering
  const filteredTasks = tasks.filter((t) => {
    if (search.trim() && !t.title.toLowerCase().includes(search.toLowerCase())) {
      return false;
    }
    if (statusFilter !== 'ALL' && t.status !== statusFilter) {
      return false;
    }
    if (priorityFilter !== 'ALL' && t.priority !== priorityFilter) {
      return false;
    }
    return true;
  });

  const handleOpenCreate = () => {
    setEditingTask(null);
    setIsModalOpen(true);
  };

  const handleOpenEdit = (task: Task) => {
    setEditingTask(task);
    setIsModalOpen(true);
  };

  const handleModalSubmit = (data: any) => {
    if (editingTask) {
      updateTaskMutation.mutate({ taskId: editingTask.id, data });
    } else {
      createTaskMutation.mutate(data);
    }
  };

  if (isProjectLoading || isTasksLoading) {
    return (
      <div className="py-24 flex justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (projectError || !project) {
    return (
      <div className="rounded-xl bg-red-50 p-8 text-center border border-red-200">
        <h3 className="text-base font-semibold text-red-800">Project Not Found</h3>
        <p className="mt-1 text-sm text-red-600">
          You may not have permission to view this project or it may have been deleted.
        </p>
        <div className="mt-4">
          <Link to="/dashboard">
            <Button variant="outline" size="sm">
              &larr; Back to Dashboard
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Back button & Title Header */}
      <div>
        <Link
          to="/dashboard"
          className="inline-flex items-center text-xs font-semibold text-gray-500 hover:text-blue-600 mb-3 transition-colors"
        >
          <svg className="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Projects
        </Link>

        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-gray-900">{project.name}</h1>
            {project.description && (
              <p className="text-sm text-gray-500 mt-1 max-w-2xl">{project.description}</p>
            )}
          </div>
          <Button variant="primary" onClick={handleOpenCreate}>
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" />
            </svg>
            Add Task
          </Button>
        </div>
      </div>

      {/* Summary Stat Cards */}
      {summary && (
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
          <div className="bg-white rounded-xl p-3.5 border border-gray-200/80 shadow-2xs">
            <span className="text-[11px] font-semibold uppercase text-gray-400">Total Tasks</span>
            <p className="text-xl font-bold text-gray-900 mt-0.5">{summary.totalTasks}</p>
          </div>
          <div className="bg-white rounded-xl p-3.5 border border-gray-200/80 shadow-2xs">
            <span className="text-[11px] font-semibold uppercase text-gray-400">To Do</span>
            <p className="text-xl font-bold text-gray-700 mt-0.5">{summary.todoTasks}</p>
          </div>
          <div className="bg-white rounded-xl p-3.5 border border-gray-200/80 shadow-2xs">
            <span className="text-[11px] font-semibold uppercase text-blue-500">In Progress</span>
            <p className="text-xl font-bold text-blue-600 mt-0.5">{summary.inProgressTasks}</p>
          </div>
          <div className="bg-white rounded-xl p-3.5 border border-gray-200/80 shadow-2xs">
            <span className="text-[11px] font-semibold uppercase text-emerald-500">Done</span>
            <p className="text-xl font-bold text-emerald-600 mt-0.5">{summary.doneTasks}</p>
          </div>
          <div className="bg-white rounded-xl p-3.5 border border-gray-200/80 shadow-2xs col-span-2 sm:col-span-1">
            <span className="text-[11px] font-semibold uppercase text-rose-500">Overdue</span>
            <p className={`text-xl font-bold mt-0.5 ${summary.overdueTasks > 0 ? 'text-rose-600' : 'text-gray-400'}`}>
              {summary.overdueTasks}
            </p>
          </div>
        </div>
      )}

      {/* Filter and View Controls */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 flex flex-col md:flex-row justify-between items-stretch md:items-center gap-3">
        {/* Search */}
        <div className="relative flex-1 max-w-xs">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg className="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <input
            type="text"
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500 focus:bg-white transition-colors"
            placeholder="Search tasks..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        {/* Filters & View Switcher */}
        <div className="flex flex-wrap items-center gap-2.5">
          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-1.5 text-gray-700 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="TODO">To Do</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="DONE">Done</option>
          </select>

          {/* Priority Filter */}
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-1.5 text-gray-700 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            <option value="ALL">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>

          {/* View Mode Toggle */}
          <div className="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-0.5">
            <button
              onClick={() => setViewMode('kanban')}
              className={`px-2.5 py-1 text-xs font-medium rounded-md transition-all ${
                viewMode === 'kanban'
                  ? 'bg-white text-gray-900 shadow-2xs'
                  : 'text-gray-500 hover:text-gray-900'
              }`}
            >
              Kanban
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`px-2.5 py-1 text-xs font-medium rounded-md transition-all ${
                viewMode === 'list'
                  ? 'bg-white text-gray-900 shadow-2xs'
                  : 'text-gray-500 hover:text-gray-900'
              }`}
            >
              List
            </button>
          </div>
        </div>
      </div>

      {/* Main View: Kanban or List */}
      {viewMode === 'kanban' ? (
        <KanbanBoard
          tasks={filteredTasks}
          onEditTask={handleOpenEdit}
          onDeleteTask={(taskId) => deleteTaskMutation.mutate(taskId)}
          onStatusChange={(taskId, status) => updateStatusMutation.mutate({ taskId, status })}
        />
      ) : (
        <TaskListView
          tasks={filteredTasks}
          onEditTask={handleOpenEdit}
          onDeleteTask={(taskId) => deleteTaskMutation.mutate(taskId)}
          onStatusChange={(taskId, status) => updateStatusMutation.mutate({ taskId, status })}
        />
      )}

      {/* Task Modal */}
      <TaskModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingTask(null);
        }}
        onSubmit={handleModalSubmit}
        initialTask={editingTask}
        isLoading={createTaskMutation.isPending || updateTaskMutation.isPending}
      />
    </div>
  );
};
