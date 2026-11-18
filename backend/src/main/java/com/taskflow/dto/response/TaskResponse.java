package com.taskflow.dto.response;

import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private boolean isOverdue;
    private Long projectId;
    private String projectName;
    private Long assigneeId;
    private String assigneeName;
    private String assigneeEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
