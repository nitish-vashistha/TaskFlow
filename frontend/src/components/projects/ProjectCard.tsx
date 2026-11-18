import React from 'react';
import { Link } from 'react-router-dom';
import { Project } from '../../types';
import { Button } from '../common/Button';

interface ProjectCardProps {
  project: Project;
  onDelete: (id: number) => void;
  isDeleting?: boolean;
}

export const ProjectCard: React.FC<ProjectCardProps> = ({ project, onDelete, isDeleting }) => {
  const percentComplete = project.totalTasks > 0
    ? Math.round((project.completedTasks / project.totalTasks) * 100)
    : 0;

  return (
    <div className="bg-white rounded-xl border border-gray-200/90 shadow-xs hover:shadow-md transition-all duration-200 flex flex-col justify-between overflow-hidden">
      <div className="p-6">
        <div className="flex justify-between items-start mb-2">
          <Link
            to={`/projects/${project.id}`}
            className="text-lg font-bold text-gray-900 hover:text-blue-600 transition-colors line-clamp-1"
          >
            {project.name}
          </Link>
          <button
            onClick={() => {
              if (window.confirm(`Are you sure you want to delete "${project.name}" and all its tasks?`)) {
                onDelete(project.id);
              }
            }}
            disabled={isDeleting}
            className="text-gray-400 hover:text-red-600 p-1 rounded-md transition-colors"
            title="Delete project"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>

        <p className="text-sm text-gray-500 line-clamp-2 min-h-[40px] mb-4">
          {project.description || 'No description provided.'}
        </p>

        {/* Progress Bar */}
        <div className="space-y-1.5">
          <div className="flex justify-between text-xs font-medium text-gray-600">
            <span>Progress</span>
            <span>{percentComplete}%</span>
          </div>
          <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden">
            <div
              className="bg-blue-600 h-2 rounded-full transition-all duration-300"
              style={{ width: `${percentComplete}%` }}
            />
          </div>
        </div>
      </div>

      <div className="px-6 py-3.5 bg-gray-50/70 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
        <div>
          <span className="font-semibold text-gray-700">{project.totalTasks}</span> tasks
          <span className="mx-1.5">•</span>
          <span className="font-semibold text-gray-700">{project.completedTasks}</span> completed
        </div>
        <Link to={`/projects/${project.id}`}>
          <Button variant="ghost" size="sm" className="text-blue-600 hover:text-blue-700 p-0 hover:bg-transparent">
            Open Board &rarr;
          </Button>
        </Link>
      </div>
    </div>
  );
};
