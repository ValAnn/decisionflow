package com.decisiontool.decisionflow;

import com.decisiontool.decisionflow.entities.Department;
import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Указывает Spring Boot поднять контекст для теста
@Transactional  // Откатит все изменения в БД после завершения теста, чтобы база оставалась чистой
public class TaskSyncIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testSaveTaskWithDepartmentAndDeadline() {
        // 1. Имитируем сырые данные, как будто они пришли из Jira API v3
        Map<String, Object> fields = new HashMap<>();
        fields.put("summary", "Тестовый импорт задачи для диплома");
        fields.put("duedate", "2026-05-30"); // Строка даты из Jira

        // 2. Создаем объект задачи и заполняем его с учетом исправленных аннотаций
        Task task = new Task();
        task.setExternalJiraId("KAN-TEST");
        task.setTitle((String) fields.get("summary"));
        task.setStatus("OPEN");

        // Наша логика перевода даты, которую мы исправляли
        String duedateStr = (String) fields.get("duedate");
        if (duedateStr != null) {
            task.setDeadlineAt(LocalDate.parse(duedateStr).atStartOfDay());
        }

        // 3. Сохраняем в репозиторий (проверяем, что Hibernate не падает)
        Task savedTask = taskRepository.save(task);

        // 4. Проверяем результаты (Assertions)
        assertNotNull(savedTask.getId(), "ID должен быть сгенерирован базой данных");
        assertEquals("KAN-TEST", savedTask.getExternalJiraId());
        assertEquals(LocalDate.of(2026, 5, 30).atStartOfDay(), savedTask.getDeadlineAt());
    }
}