package com.taskflow.controller;

import com.taskflow.dto.request.AssignTaskRequest;
import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.request.UpdateTaskStatusRequest;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.security.UserPrincipal;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TaskResponse response = taskService.createTask(projectId, request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TaskResponse>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PagedResponse<TaskResponse> response = taskService.getTasks(
                projectId, status, priority, assigneeId, search, sortBy, sortDir, page, size, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kanban")
    public ResponseEntity<List<TaskResponse>> getAllTasksForKanban(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<TaskResponse> response = taskService.getAllTasksForKanban(projectId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.getTaskById(projectId, id, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.updateTask(projectId, id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        taskService.deleteTask(projectId, id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.updateTaskStatus(projectId, id, request, currentUser));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody AssignTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.assignTask(projectId, id, request, currentUser));
    }
}
