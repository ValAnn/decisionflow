package com.decisiontool.decisionflow.services;

import com.decisiontool.decisionflow.dtos.TaskCreateDto;
import com.decisiontool.decisionflow.entities.Department;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.entities.Skill;
import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.DepartmentRepository;
import com.decisiontool.decisionflow.repositories.SkillRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import com.decisiontool.decisionflow.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final MatchingService matchingService;
    private final JiraIntegrationService jiraIntegrationService;

    private final SkillRepository skillRepository;

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
        boolean isOwner = task.getAnalyst().getUsername().equals(username);
        //boolean isLead = task.getLead() != null && task.getLead().getUsername().equals(username);

        if (!isOwner) {
            throw new AccessDeniedException("У вас нет прав на изменение этой задачи");
        }

        // 3. Валидация статуса
        validateStatus(newStatus);

        // 4. Обновление
        task.setStatus(newStatus);

        if (task.getExternalJiraId() != null && !task.getExternalJiraId().isBlank()) {
            try {
                // Вызываем метод из твоего JiraIntegrationService
                jiraIntegrationService.updateIssueStatus(task.getExternalJiraId(), newStatus);
                
            } catch (Exception e) {
                // Логируем ошибку, чтобы из-за упавшей Jira не ломалась локальная база данных
                // log.error("Локальный статус изменен, но не удалось обновить статус в Jira для задачи {}", task.getExternalJiraId(), e);
            }
        }
        
        // Если задача перешла в DONE, можно зафиксировать время завершения
        if ("DONE".equalsIgnoreCase(newStatus)) {
             task.setCompletedAt(LocalDateTime.now()); 
        }

        return taskRepository.save(task);
    }

    private void validateStatus(String status) {
        List<String> validStatuses = Arrays.asList("СОЗДАНО", "ИССЛЕДОВАНИЕ", "К ВЫПОЛНЕНИЮ");
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
        User currentAnalyst = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Аналитик не найден"));

        List<Map<String, Object>> jiraIssues = jiraIntegrationService.getIssuesForAnalyst(currentAnalyst.getJiraAccountId());

        for (Map<String, Object> issue : jiraIssues) {
            String key = (String) issue.get("key");
            Map<String, Object> fields = (Map<String, Object>) issue.get("fields");

            // Ищем существующую задачу по ключу или создаем новую
            Task task = taskRepository.findByExternalJiraId(key)
                    .orElseGet(() -> {
                        Task t = new Task();
                        t.setExternalJiraId(key);
                        t.setAnalyst(currentAnalyst);
                        return t;
                    });

            // Используем общий метод маппинга
            mapJiraFieldsToTask(task, fields);
            
            taskRepository.save(task);
        }

    }

    @Transactional
public Task syncSingleTaskWithJira(Long taskId) {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

    // // Получаем данные из Jira по ключу (например, KAN-1)
    Map<String, Object> response = jiraIntegrationService.getIssue(task.getExternalJiraId());
    Map<String, Object> fields = (Map<String, Object>) response.get("fields");

    // // Используем ТОТ ЖЕ самый метод маппинга
    mapJiraFieldsToTask(task, fields);

    return taskRepository.save(task);
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
        
        for (Map<String, Object> paragraph : content) {
            List<Map<String, Object>> innerContent = (List<Map<String, Object>>) paragraph.get("content");
            if (innerContent != null) {
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

private void mapJiraFieldsToTask(Task task, Map<String, Object> fields) {
    // 1. Основные текстовые поля
    task.setTitle((String) fields.get("summary"));
    task.setDescription(parseJiraDescription(fields.get("description")));
    
    // 2. Статус
    Map<String, Object> statusMap = (Map<String, Object>) fields.get("status");
    if (statusMap != null) {
        task.setStatus(((String) statusMap.get("name")).toUpperCase());
    }

    //Приоритет
    Map<String, Object> priority = (Map<String, Object>) fields.get("priority");
    if (priority != null) {
        task.setPriority(((String) priority.get("name")).toUpperCase());
    }

    String duedateStr = (String) fields.get("duedate"); // В Jira это просто строка
    if (duedateStr != null && !duedateStr.isEmpty()) {
        // Парсим строку "2026-05-30" в LocalDate
        LocalDate date = LocalDate.parse(duedateStr);
        
        // Если твое поле в Task — LocalDateTime, добавляем начало дня
        task.setDeadlineAt(date.atStartOfDay()); 
    }

    // 3. Департамент и Специализация (наши кастомные поля)
    Map<String, Object> departament = (Map<String, Object>) fields.get("customfield_10072");
    if (departament != null) {
        // Здесь можно либо сетить строку, либо искать в таблице departments
        // Для простоты пока предположим, что в Task есть строковое поле
        task.setDepartment(departmentRepository.findByName((String) departament.get("value")));
    }

    Map<String, Object> dept = (Map<String, Object>) fields.get("customfield_10073");
    if (dept != null) {
        // Здесь можно либо сетить строку, либо искать в таблице departments
        // Для простоты пока предположим, что в Task есть строковое поле
        task.setRequiredSpecialization((String) dept.get("value")); 
    }

    // 4. Синхронизация скиллов (Labels) — Твоя новая связь Many-to-Many
    List<String> labels = (List<String>) fields.get("labels");
    if (labels != null) {
        Set<Skill> taskSkills = labels.stream()
            .map(label -> skillRepository.findByNameIgnoreCase(label)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(label);
                    newSkill.setCategory("Jira Import");
                    return skillRepository.save(newSkill);
                }))
            .collect(Collectors.toSet());
        task.setSkills(taskSkills);
    }
}
public Task updateExistingTask(Long id, TaskCreateDto dto) {
        //log.info("Обновление задачи с ID: {}", id);

        // 1. Ищем существующую задачу в БД
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача не найдена с ID: " + id));

        // 2. Обновляем простые текстовые поля
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setRequiredSpecialization(dto.getRequiredSpecialization());
        task.setUpdatedAt(LocalDateTime.now());

        if (dto.getDeadlineAt() != null) {
            task.setDeadlineAt(LocalDateTime.parse(dto.getDeadlineAt()));
        }

        // 3. Обновляем департамент
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Департамент не найден"));
            task.setDepartment(dept);
        }

        // 4. Обновляем разработчика (исполнителя, которого подобрала СППР)
        if (dto.getDeveloper() != null && dto.getDeveloper().getId() != null) {
            User dev = userRepository.findById(dto.getDeveloper().getId())
                    .orElseThrow(() -> new RuntimeException("Разработчик не найден"));
            task.setAssignee(dev);
        } else {
            task.setAssignee(null); // Если исполнителя сняли
        }

        // 5. Обновляем навыки задачи (skills)
        if (dto.getSkills() != null) {
    java.util.Set<Skill> updatedSkills = dto.getSkills().stream()
            .map(skillDto -> {
                if (skillDto.getId() != null) {
                    return skillRepository.findById(skillDto.getId())
                            .orElseThrow(() -> new RuntimeException("Навык не найден"));
                } else {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillDto.getName());
                    return skillRepository.save(newSkill);
                }
            })
            .collect(java.util.stream.Collectors.toSet()); // <-- Вот этот фикс!

    task.setSkills(updatedSkills); 
        }// Теперь типы идеально совпадают

    List<String> skillNames = dto.getSkills() != null ? 
    dto.getSkills().stream().map(s -> s.getName().replaceAll("\\s+", "_")).toList() : // Метки в Jira не любят пробелы
    Collections.emptyList();

    String devJiraId = (task.getAssignee() != null) ? task.getAssignee().getJiraAccountId() : null;

        // 6. СИНХРОНИЗАЦИЯ С JIRA: Обновляем данные в существующем тикете Jira
        if (task.getExternalJiraId() != null && !task.getExternalJiraId().contains("ERROR")) {
            try {
                // Отправляем обновленное название и описание по ключу таски (например, KAN-6)
                jiraIntegrationService.updateIssue(
                    task.getExternalJiraId(),         // Ключ таски (например, "KAN-1")
                    dto.getTitle(),           // Новое название
                    dto.getDescription(),     // Новое описание
                    dto.getPriority(),        // Приоритет
                    skillNames,               // Навыки (skills) как метки
                    devJiraId                 // Jira account ID назначенного разработчика
                );
                //log.info("Задача успешно обновлена в Jira по ключу: {}", task.getJiraId());
            } catch (Exception e) {
                //log.error("Не удалось обновить данные в Jira для задачи {}", task.getJiraId(), e);
            }
        }

        // 7. Сохраняем обновленную сущность в локальную базу данных
        return taskRepository.save(task);
    }
}
