package com.taskflow.dto.request;

import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 255, message = "Task title must be between 1 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate dueDate;

    private Long assigneeId;
}
