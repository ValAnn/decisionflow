package com.decisiontool.decisionflow.services;

import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.repositories.TaskRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final MatchingService matchingService;

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Пример использования нашего интеллектуального модуля
     */
    public void getRecommendation(Long taskId, Long devId) {
        double score = matchingService.predictMatchingScore(taskId, devId);
        System.out.println("Рекомендация для задачи: " + score + "%");
    }

    public List<Task> getTaskByUsername(String username){
        return taskRepository.findAllByAssigneeUsername(username);
    }

    @Transactional // Важно для изменения данных
    public Task changeTaskStatus(Long taskId, String newStatus, String username) {
        // 1. Ищем задачу
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // 2. Бизнес-проверка: может ли этот юзер менять статус?
        boolean isOwner = task.getAssignee().getUsername().equals(username);
        boolean isLead = task.getLead() != null && task.getLead().getUsername().equals(username);

        if (!isOwner && !isLead) {
            throw new AccessDeniedException("У вас нет прав на изменение этой задачи");
        }

        // 3. Валидация статуса
        validateStatus(newStatus);

        // 4. Обновление
        task.setStatus(newStatus);
        
        // Если задача перешла в DONE, можно зафиксировать время завершения
        if ("DONE".equals(newStatus)) {
            // task.setCompletedAt(LocalDateTime.now()); 
        }

        return taskRepository.save(task);
    }

    private void validateStatus(String status) {
        List<String> validStatuses = Arrays.asList("TODO", "IN_PROGRESS", "DONE");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Некорректный статус: " + status);
        }
    }

    public Task getTaskById(Long id) {
    return taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + id + " не найдена"));
}
}