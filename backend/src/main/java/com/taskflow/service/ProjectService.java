package com.taskflow.service;

import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.dto.response.ProjectSummaryResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.enums.TaskStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UserPrincipal currentUser) {
        User owner = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .owner(owner)
                .build();

        Project saved = projectRepository.save(project);
        return mapToResponse(saved, 0, 0);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(UserPrincipal currentUser) {
        List<Project> projects = projectRepository.findByOwnerId(currentUser.getId());
        return projects.stream()
                .map(p -> {
                    long totalTasks = taskRepository.countByProjectId(p.getId());
                    long completedTasks = taskRepository.countByProjectIdAndStatus(p.getId(), TaskStatus.DONE);
                    return mapToResponse(p, totalTasks, completedTasks);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, UserPrincipal currentUser) {
        Project project = getProjectAndValidateOwnership(projectId, currentUser.getId());
        long totalTasks = taskRepository.countByProjectId(project.getId());
        long completedTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        return mapToResponse(project, totalTasks, completedTasks);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, UserPrincipal currentUser) {
        Project project = getProjectAndValidateOwnership(projectId, currentUser.getId());
        project.setName(request.getName().trim());
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription().trim());
        } else {
            project.setDescription(null);
        }

        Project updated = projectRepository.save(project);
        long totalTasks = taskRepository.countByProjectId(updated.getId());
        long completedTasks = taskRepository.countByProjectIdAndStatus(updated.getId(), TaskStatus.DONE);
        return mapToResponse(updated, totalTasks, completedTasks);
    }

    @Transactional
    public void deleteProject(Long projectId, UserPrincipal currentUser) {
        Project project = getProjectAndValidateOwnership(projectId, currentUser.getId());
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public ProjectSummaryResponse getProjectSummary(Long projectId, UserPrincipal currentUser) {
        Project project = getProjectAndValidateOwnership(projectId, currentUser.getId());

        long total = taskRepository.countByProjectId(project.getId());
        long todo = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.TODO);
        long inProgress = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        long overdue = taskRepository.countByProjectIdAndStatusNotAndDueDateBefore(
                project.getId(), TaskStatus.DONE, LocalDate.now());

        return ProjectSummaryResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(total)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .doneTasks(done)
                .overdueTasks(overdue)
                .build();
    }

    public Project getProjectAndValidateOwnership(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to access this project");
        }
        return project;
    }

    private ProjectResponse mapToResponse(Project project, long totalTasks, long completedTasks) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .ownerName(project.getOwner().getFullName())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .build();
    }
}
