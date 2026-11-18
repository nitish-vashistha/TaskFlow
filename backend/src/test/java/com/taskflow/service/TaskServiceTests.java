package com.taskflow.service;

import com.taskflow.dto.request.AssignTaskRequest;
import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.request.UpdateTaskStatusRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User owner;
    private UserPrincipal ownerPrincipal;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .fullName("Project Owner")
                .password("hash")
                .build();

        ownerPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("owner@example.com")
                .fullName("Project Owner")
                .authorities(Collections.emptyList())
                .build();

        project = Project.builder()
                .id(10L)
                .name("Alpha Project")
                .owner(owner)
                .build();

        task = Task.builder()
                .id(100L)
                .title("Initial Task")
                .description("Task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(3))
                .project(project)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Feature Implementation")
                .description("Build the user dashboard")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        when(projectService.getProjectAndValidateOwnership(10L, 1L)).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(101L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        TaskResponse response = taskService.createTask(10L, request, ownerPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getTitle()).isEqualTo("New Feature Implementation");
        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("Should update task status successfully")
    void testUpdateTaskStatus() {
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(projectService.getProjectAndValidateOwnership(10L, 1L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTaskStatus(10L, 100L, request, ownerPrincipal);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should assign task to user")
    void testAssignTask() {
        User assignee = User.builder()
                .id(2L)
                .email("member@example.com")
                .fullName("Team Member")
                .build();

        AssignTaskRequest request = AssignTaskRequest.builder()
                .assigneeId(2L)
                .build();

        when(projectService.getProjectAndValidateOwnership(10L, 1L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.assignTask(10L, 100L, request, ownerPrincipal);

        assertThat(response.getAssigneeId()).isEqualTo(2L);
        assertThat(response.getAssigneeName()).isEqualTo("Team Member");
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask() {
        when(projectService.getProjectAndValidateOwnership(10L, 1L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));

        taskService.deleteTask(10L, 100L, ownerPrincipal);

        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("Should correctly identify overdue task")
    void testOverdueTaskDetection() {
        // Due yesterday, still in TODO -> overdue
        Task overdueTask = Task.builder()
                .id(102L)
                .title("Past Due Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(1))
                .project(project)
                .build();

        TaskResponse overdueResponse = taskService.mapToResponse(overdueTask);
        assertThat(overdueResponse.isOverdue()).isTrue();

        // Completed task past due date -> NOT overdue
        Task completedTask = Task.builder()
                .id(103L)
                .title("Completed Task")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(1))
                .project(project)
                .build();

        TaskResponse completedResponse = taskService.mapToResponse(completedTask);
        assertThat(completedResponse.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("Should fetch all tasks for Kanban board")
    void testGetAllTasksForKanban() {
        when(projectService.getProjectAndValidateOwnership(10L, 1L)).thenReturn(project);
        when(taskRepository.findByProjectId(10L)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getAllTasksForKanban(10L, ownerPrincipal);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Initial Task");
    }
}
