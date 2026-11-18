import { apiClient } from './client';
import { CreateTaskRequest, PagedResponse, Task, TaskFilterParams, TaskStatus, UpdateTaskRequest } from '../types';

export const tasksApi = {
  getTasks: async (projectId: number, params?: TaskFilterParams): Promise<PagedResponse<Task>> => {
    const res = await apiClient.get<PagedResponse<Task>>(`/projects/${projectId}/tasks`, { params });
    return res.data;
  },

  getKanbanTasks: async (projectId: number): Promise<Task[]> => {
    const res = await apiClient.get<Task[]>(`/projects/${projectId}/tasks/kanban`);
    return res.data;
  },

  getTaskById: async (projectId: number, taskId: number): Promise<Task> => {
    const res = await apiClient.get<Task>(`/projects/${projectId}/tasks/${taskId}`);
    return res.data;
  },

  createTask: async (projectId: number, data: CreateTaskRequest): Promise<Task> => {
    const res = await apiClient.post<Task>(`/projects/${projectId}/tasks`, data);
    return res.data;
  },

  updateTask: async (projectId: number, taskId: number, data: UpdateTaskRequest): Promise<Task> => {
    const res = await apiClient.put<Task>(`/projects/${projectId}/tasks/${taskId}`, data);
    return res.data;
  },

  deleteTask: async (projectId: number, taskId: number): Promise<void> => {
    await apiClient.delete(`/projects/${projectId}/tasks/${taskId}`);
  },

  updateTaskStatus: async (projectId: number, taskId: number, status: TaskStatus): Promise<Task> => {
    const res = await apiClient.patch<Task>(`/projects/${projectId}/tasks/${taskId}/status`, { status });
    return res.data;
  },

  assignTask: async (projectId: number, taskId: number, assigneeId?: number | null): Promise<Task> => {
    const res = await apiClient.patch<Task>(`/projects/${projectId}/tasks/${taskId}/assign`, { assigneeId });
    return res.data;
  },
};
