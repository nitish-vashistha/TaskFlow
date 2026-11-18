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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTests {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectService projectService;

    private User owner;
    private UserPrincipal ownerPrincipal;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .fullName("Project Owner")
                .password("hashedPassword")
                .build();

        ownerPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("owner@example.com")
                .fullName("Project Owner")
                .authorities(Collections.emptyList())
                .build();

        project = Project.builder()
                .id(10L)
                .name("Core Platform")
                .description("Platform revamp")
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create project successfully")
    void testCreateProject() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Architecture")
                .description("System upgrade")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(11L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        ProjectResponse response = projectService.createProject(request, ownerPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getName()).isEqualTo("New Architecture");
        assertThat(response.getOwnerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should retrieve projects for the current user")
    void testGetUserProjects() {
        when(projectRepository.findByOwnerId(1L)).thenReturn(List.of(project));
        when(taskRepository.countByProjectId(10L)).thenReturn(5L);
        when(taskRepository.countByProjectIdAndStatus(10L, TaskStatus.DONE)).thenReturn(2L);

        List<ProjectResponse> responses = projectService.getUserProjects(ownerPrincipal);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Core Platform");
        assertThat(responses.get(0).getTotalTasks()).isEqualTo(5L);
        assertThat(responses.get(0).getCompletedTasks()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should get project by ID when user is owner")
    void testGetProjectByIdSuccess() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskRepository.countByProjectId(10L)).thenReturn(3L);
        when(taskRepository.countByProjectIdAndStatus(10L, TaskStatus.DONE)).thenReturn(1L);

        ProjectResponse response = projectService.getProjectById(10L, ownerPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not project owner")
    void testGetProjectUnauthorized() {
        UserPrincipal anotherUser = UserPrincipal.builder()
                .id(2L)
                .email("another@example.com")
                .authorities(Collections.emptyList())
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.getProjectById(10L, anotherUser))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("You do not have permission to access this project");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project does not exist")
    void testGetProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L, ownerPrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found with id: 99");
    }

    @Test
    @DisplayName("Should update project details")
    void testUpdateProject() {
        UpdateProjectRequest updateReq = UpdateProjectRequest.builder()
                .name("Core Platform V2")
                .description("Updated details")
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse updated = projectService.updateProject(10L, updateReq, ownerPrincipal);

        assertThat(updated.getName()).isEqualTo("Core Platform V2");
        assertThat(updated.getDescription()).isEqualTo("Updated details");
    }

    @Test
    @DisplayName("Should delete project successfully")
    void testDeleteProject() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        projectService.deleteProject(10L, ownerPrincipal);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("Should calculate project summary with overdue count")
    void testGetProjectSummary() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskRepository.countByProjectId(10L)).thenReturn(10L);
        when(taskRepository.countByProjectIdAndStatus(10L, TaskStatus.TODO)).thenReturn(4L);
        when(taskRepository.countByProjectIdAndStatus(10L, TaskStatus.IN_PROGRESS)).thenReturn(3L);
        when(taskRepository.countByProjectIdAndStatus(10L, TaskStatus.DONE)).thenReturn(3L);
        when(taskRepository.countByProjectIdAndStatusNotAndDueDateBefore(eq(10L), eq(TaskStatus.DONE), any(LocalDate.class)))
                .thenReturn(2L);

        ProjectSummaryResponse summary = projectService.getProjectSummary(10L, ownerPrincipal);

        assertThat(summary.getTotalTasks()).isEqualTo(10L);
        assertThat(summary.getTodoTasks()).isEqualTo(4L);
        assertThat(summary.getInProgressTasks()).isEqualTo(3L);
        assertThat(summary.getDoneTasks()).isEqualTo(3L);
        assertThat(summary.getOverdueTasks()).isEqualTo(2L);
    }
}
