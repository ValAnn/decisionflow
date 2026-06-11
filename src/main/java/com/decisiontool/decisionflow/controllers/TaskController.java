package com.decisiontool.decisionflow.controllers;
import com.decisiontool.decisionflow.dtos.TaskCreateDto;
import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    @GetMapping("/my")
    public ResponseEntity<?> getMyTasks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }
        String username = authentication.getName();
        return ResponseEntity.ok(taskService.getTasksByAnalyst(username));
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        String newStatus = payload.get("status");
        String currentUsername = authentication.getName();
        Task updatedTask = taskService.changeTaskStatus(id, newStatus, currentUsername);
        return ResponseEntity.ok(updatedTask);
    }
    @GetMapping("/{id}")
public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
    Task task = taskService.getTaskById(id);
    return ResponseEntity.ok(task);
}
    @PostMapping("/import")
    public ResponseEntity<String> importTasks(Authentication authentication) {
        taskService.importTasksFromJira(authentication.getName());
        return ResponseEntity.ok("Синхронизация успешно завершена");
    }
    @PostMapping("/{id}/sync-jira")
    public ResponseEntity<Task> syncTaskWithJira(@PathVariable("id") Long id) {
        Task importedTask = taskService.syncSingleTaskWithJira(id);
        return ResponseEntity.ok(importedTask);
    }
    @PostMapping("/{id}/complete-analysis")
    public ResponseEntity<Void> completeAnalysis(
            @PathVariable("id") Long id,
            @RequestParam Long developerId) {
        taskService.completeTaskAnalysis(id, developerId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable("id") Long id, @RequestBody TaskCreateDto dto) {
        Task updatedTask = taskService.updateExistingTask(id, dto);
        return ResponseEntity.ok(updatedTask);
    }
}
