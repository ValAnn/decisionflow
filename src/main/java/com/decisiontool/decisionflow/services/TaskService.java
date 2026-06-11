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
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));
        boolean isOwner = task.getAnalyst().getUsername().equals(username);
        if (!isOwner) {
            throw new AccessDeniedException("У вас нет прав на изменение этой задачи");
        }
        validateStatus(newStatus);
        task.setStatus(newStatus);
        if (task.getExternalJiraId() != null && !task.getExternalJiraId().isBlank()) {
            try {
                jiraIntegrationService.updateIssueStatus(task.getExternalJiraId(), newStatus);
            } catch (Exception e) {
            }
        }
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
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new EntityNotFoundException("Разработчик не найден"));
        task.setAssignee(developer);
        task.setStatus("TO_DO");
        taskRepository.save(task);
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
            Task task = taskRepository.findByExternalJiraId(key)
                    .orElseGet(() -> {
                        Task t = new Task();
                        t.setExternalJiraId(key);
                        t.setAnalyst(currentAnalyst);
                        return t;
                    });
            mapJiraFieldsToTask(task, fields);
            taskRepository.save(task);
        }
    }
    @Transactional
public Task syncSingleTaskWithJira(Long taskId) {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));
    Map<String, Object> response = jiraIntegrationService.getIssue(task.getExternalJiraId());
    Map<String, Object> fields = (Map<String, Object>) response.get("fields");
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
                fullText.append("\n");
            }
        }
        return fullText.toString().trim();
    } catch (Exception e) {
        return "";
    }
}
private void mapJiraFieldsToTask(Task task, Map<String, Object> fields) {
    task.setTitle((String) fields.get("summary"));
    task.setDescription(parseJiraDescription(fields.get("description")));
    Map<String, Object> statusMap = (Map<String, Object>) fields.get("status");
    if (statusMap != null) {
        task.setStatus(((String) statusMap.get("name")).toUpperCase());
    }
    Map<String, Object> priority = (Map<String, Object>) fields.get("priority");
    if (priority != null) {
        task.setPriority(((String) priority.get("name")).toUpperCase());
    }
    String duedateStr = (String) fields.get("duedate");
    if (duedateStr != null && !duedateStr.isEmpty()) {
        LocalDate date = LocalDate.parse(duedateStr);
        task.setDeadlineAt(date.atStartOfDay());
    }
    Map<String, Object> departament = (Map<String, Object>) fields.get("customfield_10072");
    if (departament != null) {
        task.setDepartment(departmentRepository.findByName((String) departament.get("value")));
    }
    Map<String, Object> dept = (Map<String, Object>) fields.get("customfield_10073");
    if (dept != null) {
        task.setRequiredSpecialization((String) dept.get("value"));
    }
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
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача не найдена с ID: " + id));
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setRequiredSpecialization(dto.getRequiredSpecialization());
        task.setUpdatedAt(LocalDateTime.now());
        if (dto.getDeadlineAt() != null) {
            task.setDeadlineAt(LocalDateTime.parse(dto.getDeadlineAt()));
        }
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Департамент не найден"));
            task.setDepartment(dept);
        }
        if (dto.getDeveloper() != null && dto.getDeveloper().getId() != null) {
            User dev = userRepository.findById(dto.getDeveloper().getId())
                    .orElseThrow(() -> new RuntimeException("Разработчик не найден"));
            task.setAssignee(dev);
        } else {
            task.setAssignee(null);
        }
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
            .collect(java.util.stream.Collectors.toSet());
    task.setSkills(updatedSkills);
        }
    List<String> skillNames = dto.getSkills() != null ?
    dto.getSkills().stream().map(s -> s.getName().replaceAll("\\s+", "_")).toList() :
    Collections.emptyList();
    String devJiraId = (task.getAssignee() != null) ? task.getAssignee().getJiraAccountId() : null;
        if (task.getExternalJiraId() != null && !task.getExternalJiraId().contains("ERROR")) {
            try {
                jiraIntegrationService.updateIssue(
                    task.getExternalJiraId(),
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getPriority(),
                    skillNames,
                    devJiraId
                );
            } catch (Exception e) {
            }
        }
        return taskRepository.save(task);
    }
}
