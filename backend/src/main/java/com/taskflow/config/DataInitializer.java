package com.taskflow.config;

import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data, skipping seed initialization.");
            return;
        }

        log.info("Seeding initial demo data for TaskFlow...");

        // 1. Seed Demo Users
        User alex = User.builder()
                .email("alex@example.com")
                .fullName("Alex Rivera")
                .password(passwordEncoder.encode("password123"))
                .build();
        alex = userRepository.save(alex);

        User sarah = User.builder()
                .email("sarah@example.com")
                .fullName("Sarah Chen")
                .password(passwordEncoder.encode("password123"))
                .build();
        sarah = userRepository.save(sarah);

        // 2. Seed Project 1
        Project corePlatform = Project.builder()
                .name("TaskFlow Platform V1")
                .description("Core architectural foundation, authentication, and task boards.")
                .owner(alex)
                .build();
        corePlatform = projectRepository.save(corePlatform);

        // Tasks for Project 1
        Task t1 = Task.builder()
                .title("Architect REST API and Security Filter Chain")
                .description("Configure stateless Spring Security 6 with JWT token verification.")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(5))
                .project(corePlatform)
                .assignee(alex)
                .build();

        Task t2 = Task.builder()
                .title("Build Kanban Board UI with quick transitions")
                .description("Implement 3-column board view with priority indicators and overdue alerts.")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(2))
                .project(corePlatform)
                .assignee(sarah)
                .build();

        Task t3 = Task.builder()
                .title("Add PostgreSQL database migration scripts")
                .description("Prepare production Docker Compose configuration and Flyway migrations.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .project(corePlatform)
                .assignee(alex)
                .build();

        Task t4 = Task.builder()
                .title("Security audit and penetration testing")
                .description("Review CORS policies, SQL injection resistance, and token expiration handling.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(2)) // Overdue!
                .project(corePlatform)
                .assignee(sarah)
                .build();

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);
        taskRepository.save(t4);

        // 3. Seed Project 2
        Project mobileApp = Project.builder()
                .name("Mobile App 2.0")
                .description("Companion mobile application for team task updates on iOS and Android.")
                .owner(alex)
                .build();
        mobileApp = projectRepository.save(mobileApp);

        Task m1 = Task.builder()
                .title("Design mobile navigation wireframes")
                .description("Tab bar layout and Kanban swipe gestures.")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().minusDays(1))
                .project(mobileApp)
                .assignee(sarah)
                .build();

        Task m2 = Task.builder()
                .title("Implement push notification service")
                .description("Notify assignees when task status changes.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusDays(14))
                .project(mobileApp)
                .assignee(alex)
                .build();

        taskRepository.save(m1);
        taskRepository.save(m2);

        log.info("Demo data seeded successfully! Login: alex@example.com / password123");
    }
}
