package com.decisiontool.decisionflow.services;

import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import com.decisiontool.decisionflow.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final MatchingService matchingService;
    private final JiraIntegrationService jiraIntegrationService;

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public void getRecommendation(Long taskId, Long devId) {
        double score = matchingService.predictMatchingScore(taskId, devId);
        System.out.println("Рекомендация для задачи: " + score + "%");
    }

    public List<Task> getTaskByUsername(String username){
        return taskRepository.findAllByAssigneeUsername(username);
    }

    @Transactional 
    public Task changeTaskStatus(Long taskId, String newStatus, String username) {
        // 1. Ищем задачу
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // 2. Бизнес-проверка: может ли этот юзер менять статус?
        boolean isOwner = task.getAssignee().getUsername().equals(username);
        //boolean isLead = task.getLead() != null && task.getLead().getUsername().equals(username);

        if (!isOwner) {
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

    public void completeTaskAnalysis(Long taskId, Long developerId) {
        // 1. Ищем задачу и разработчика в нашей БД
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));
        
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new EntityNotFoundException("Разработчик не найден"));

        // 2. Обновляем состояние задачи в нашей системе
        task.setAssignee(developer);
        task.setStatus("TO_DO");
        //task.setAnalyzedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Передаем jiraAccountId
    if (developer.getJiraAccountId() != null) {
        jiraIntegrationService.moveToTodo(
            task.getExternalJiraId(), 
            developer.getJiraAccountId()
        );
    } else {
        throw new RuntimeException("У разработчика не привязан Jira Account ID");
    }
    }

    @Transactional
public void importTasksFromJira(String username) {
    // 1. Находим текущего аналитика (кто делает запрос)
    User currentAnalyst = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Аналитик не найден"));

    // 2. Запрашиваем задачи у Jira
    List<Map<String, Object>> jiraIssues = jiraIntegrationService.getIssuesForAnalyst(currentAnalyst.getJiraAccountId());

    for (Map<String, Object> issue : jiraIssues) {
        String key = (String) issue.get("key"); // Например, "KAN-4"

        // Проверяем, нет ли уже такой задачи в нашей базе
        if (!taskRepository.existsByExternalJiraId(key)) {
            Map<String, Object> fields = (Map<String, Object>) issue.get("fields");
            
            Task newTask = new Task();
            newTask.setExternalJiraId(key);
            
            // Берем название задачи
            newTask.setTitle((String) fields.get("summary"));

            //description
            String descriptionText = parseJiraDescription(fields.get("description"));
            newTask.setDescription(descriptionText); // Предполагаем, что в Task есть поле description
            
            //priority
            Map<String, Object> priorityMap = (Map<String, Object>) fields.get("priority");
            String priority = (String) priorityMap.get("name");
            newTask.setPriority(priority.toUpperCase()); 

            // Извлекаем название статуса: fields -> status -> name
            Map<String, Object> statusMap = (Map<String, Object>) fields.get("status");
            if (statusMap != null) {
                String jiraStatus = (String) statusMap.get("name");
                // Можно маппить статусы Jira на свои, например:
                newTask.setStatus(jiraStatus.toUpperCase()); 
            } else {
                newTask.setStatus("TODO");
            }
            
            // Назначаем аналитика, под которым залогинены
            newTask.setAnalyst(currentAnalyst); 
            
            // Если нужно проверить Исполнителя (Assignee) из Jira
            Map<String, Object> assignee = (Map<String, Object>) fields.get("assignee");
            if (assignee != null) {
                // Если в будущем нужно будет связывать исполнителя — логика будет тут
                // String assigneeName = (String) assignee.get("displayName");
            }

            taskRepository.save(newTask);
        }
    }
}

public List<Task> getTasksByAnalyst(String username) {
    return taskRepository.findAllByAnalystUsername(username);
}

private String parseJiraDescription(Object descriptionObj) {
    if (!(descriptionObj instanceof Map)) return "";
    
    try {
        Map<String, Object> descriptionMap = (Map<String, Object>) descriptionObj;
        List<Map<String, Object>> content = (List<Map<String, Object>>) descriptionMap.get("content");
        
        if (content == null || content.isEmpty()) return "";

        StringBuilder fullText = new StringBuilder();
        
        // Проходим по параграфам
        for (Map<String, Object> paragraph : content) {
            List<Map<String, Object>> innerContent = (List<Map<String, Object>>) paragraph.get("content");
            if (innerContent != null) {
                // Собираем текст из каждого элемента параграфа
                for (Map<String, Object> textElement : innerContent) {
                    if ("text".equals(textElement.get("type"))) {
                        fullText.append((String) textElement.get("text"));
                    }
                }
                fullText.append("\n"); // Добавляем перенос строки между параграфами
            }
        }
        return fullText.toString().trim();
    } catch (Exception e) {
        // Если структура изменится или будет пустой, возвращаем пустую строку
        return "";
    }
}
}