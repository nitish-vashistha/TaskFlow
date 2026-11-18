import { apiClient } from './client';
import { CreateProjectRequest, Project, ProjectSummary, UpdateProjectRequest } from '../types';

export const projectsApi = {
  getProjects: async (): Promise<Project[]> => {
    const res = await apiClient.get<Project[]>('/projects');
    return res.data;
  },

  getProjectById: async (id: number): Promise<Project> => {
    const res = await apiClient.get<Project>(`/projects/${id}`);
    return res.data;
  },

  createProject: async (data: CreateProjectRequest): Promise<Project> => {
    const res = await apiClient.post<Project>('/projects', data);
    return res.data;
  },

  updateProject: async (id: number, data: UpdateProjectRequest): Promise<Project> => {
    const res = await apiClient.put<Project>(`/projects/${id}`, data);
    return res.data;
  },

  deleteProject: async (id: number): Promise<void> => {
    await apiClient.delete(`/projects/${id}`);
  },

  getProjectSummary: async (id: number): Promise<ProjectSummary> => {
    const res = await apiClient.get<ProjectSummary>(`/projects/${id}/summary`);
    return res.data;
  },
};
