package com.taskflow.service;

import com.taskflow.dto.request.AssignTaskRequest;
import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.request.UpdateTaskStatusRequest;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.TaskSpecification;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, UserPrincipal currentUser) {
        Project project = projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with id: " + request.getAssigneeId()));
        }

        Task task = Task.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .project(project)
                .assignee(assignee)
                .build();

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasks(
            Long projectId,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId,
            String search,
            String sortBy,
            String sortDir,
            int page,
            int size,
            UserPrincipal currentUser) {

        projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "createdAt"
        );
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Specification<Task> spec = TaskSpecification.filterTasks(projectId, status, priority, assigneeId, search);
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        List<TaskResponse> content = taskPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<TaskResponse>builder()
                .content(content)
                .page(taskPage.getNumber())
                .size(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasksForKanban(Long projectId, UserPrincipal currentUser) {
        projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long projectId, Long taskId, UserPrincipal currentUser) {
        projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());
        Task task = getTaskAndValidateProject(taskId, projectId);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, UpdateTaskRequest request, UserPrincipal currentUser) {
        projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());
        Task task = getTaskAndValidateProject(taskId, projectId);

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId, UserPrincipal currentUser) {
        projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());
        Task task = getTaskAndValidateProject(taskId, projectId);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long projectId, Long taskId, UpdateTaskStatusRequest request, UserPrincipal currentUser) {
        projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());
        Task task = getTaskAndValidateProject(taskId, projectId);
        task.setStatus(request.getStatus());
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public TaskResponse assignTask(Long projectId, Long taskId, AssignTaskRequest request, UserPrincipal currentUser) {
        projectService.getProjectAndValidateOwnership(projectId, currentUser.getId());
        Task task = getTaskAndValidateProject(taskId, projectId);

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    private Task getTaskAndValidateProject(Long taskId, Long projectId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId + " in project: " + projectId));
    }

    public TaskResponse mapToResponse(Task task) {
        boolean isOverdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .isOverdue(isOverdue)
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
                .assigneeEmail(task.getAssignee() != null ? task.getAssignee().getEmail() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
