package com.decisiontool.decisionflow.controllers;

import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.services.TaskService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

    // @GetMapping("/my")
    // public ResponseEntity<List<Task>> getMyTasks(Principal principal) {
    //     // principal.getName() — это username, который мы положили в JWT
    //     return ResponseEntity.ok(taskService.getTaskByUsername(principal.getName()));
    // }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTasks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }
        String username = authentication.getName();
        return ResponseEntity.ok(taskService.getTaskByUsername(username));
    }
    
}