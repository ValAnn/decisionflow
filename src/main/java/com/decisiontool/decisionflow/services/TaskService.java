package com.decisiontool.decisionflow.services;

import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
}