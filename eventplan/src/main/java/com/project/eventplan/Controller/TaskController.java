package com.project.eventplan.Controller;

import com.project.eventplan.Dto.TaskRequest;
import com.project.eventplan.Dto.PageResponse;
import com.project.eventplan.Entity.Task;
import com.project.eventplan.Service.TaskService;
import com.project.eventplan.Util.PaginationUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Task> createTask(@RequestParam Long eventId,
                                           @RequestParam Long teamMemberId,
                                           @RequestParam Long vendorId,
                                           @Valid @RequestBody TaskRequest request) {
        Task task = new Task();
        task.setTaskName(request.taskName());
        task.setDescription(request.description());
        return ResponseEntity.ok(taskService.createTask(eventId, teamMemberId, vendorId, task));
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER')")
    @GetMapping
    public ResponseEntity<PageResponse<Task>> getTasks(@RequestParam(required = false) String query,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        List<Task> tasks = taskService.getTasksForCurrentUser();
        if (query != null && !query.isBlank()) {
            String normalized = query.toLowerCase(Locale.ROOT);
            tasks = tasks.stream()
                    .filter(task -> contains(task.getTaskName(), normalized)
                            || contains(task.getDescription(), normalized))
                    .toList();
        }
        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.toLowerCase(Locale.ROOT);
            tasks = tasks.stream()
                    .filter(task -> contains(task.getStatus(), normalizedStatus))
                    .toList();
        }
        return ResponseEntity.ok(PaginationUtils.paginate(tasks, page, size));
    }

    @PreAuthorize("hasRole('TEAM_MEMBER')")
    @PutMapping("/{taskId}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long taskId,
                                             @RequestParam String status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
