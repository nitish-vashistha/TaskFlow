package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filterTasks(
            Long projectId,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId,
            String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always scope to project
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
