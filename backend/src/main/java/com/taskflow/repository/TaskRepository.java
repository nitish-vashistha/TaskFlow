package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByProjectId(Long projectId);
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    Optional<Task> findByIdAndProjectId(Long id, Long projectId);

    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    long countByProjectIdAndStatusNotAndDueDateBefore(Long projectId, TaskStatus status, LocalDate date);
}
