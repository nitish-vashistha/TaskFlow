package com.taskflow.repository;

import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Should persist and retrieve User, Project, and Task entities")
    void testEntityPersistenceAndRelationships() {
        // 1. Create and persist User
        User owner = User.builder()
                .email("alex@example.com")
                .fullName("Alex Rivera")
                .password("$2a$10$hashedpassword")
                .build();
        User savedOwner = userRepository.save(owner);
        assertThat(savedOwner.getId()).isNotNull();
        assertThat(savedOwner.getCreatedAt()).isNotNull();

        // 2. Create and persist Project
        Project project = Project.builder()
                .name("Website Redesign")
                .description("Overhaul company landing page")
                .owner(savedOwner)
                .build();
        Project savedProject = projectRepository.save(project);
        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getOwner().getEmail()).isEqualTo("alex@example.com");

        // 3. Create and persist Task
        Task task = Task.builder()
                .title("Design mockups")
                .description("Figma board for home page")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(5))
                .project(savedProject)
                .assignee(savedOwner)
                .build();
        Task savedTask = taskRepository.save(task);
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);

        // 4. Test Queries
        List<Project> ownerProjects = projectRepository.findByOwnerId(savedOwner.getId());
        assertThat(ownerProjects).hasSize(1);

        List<Task> projectTasks = taskRepository.findByProjectId(savedProject.getId());
        assertThat(projectTasks).hasSize(1);
        assertThat(projectTasks.get(0).getTitle()).isEqualTo("Design mockups");

        long totalTasks = taskRepository.countByProjectId(savedProject.getId());
        assertThat(totalTasks).isEqualTo(1);

        long doneTasks = taskRepository.countByProjectIdAndStatus(savedProject.getId(), TaskStatus.DONE);
        assertThat(doneTasks).isZero();
    }

    @Test
    @DisplayName("Should check email existence in UserRepository")
    void testUserExistence() {
        User user = User.builder()
                .email("unique@example.com")
                .fullName("Unique User")
                .password("secret")
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("unique@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }
}
