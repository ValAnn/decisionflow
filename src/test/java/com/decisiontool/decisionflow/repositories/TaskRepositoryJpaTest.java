package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Поднимает облегченный контекст только для работы с БД (использует h2 или тестовую базу)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepositoryJpaTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testFindByStatus_ShouldReturnCorrectTasks() {
        // 1. Создаем и сохраняем тестовую задачу со статусом "IN_PROGRESS"
        Task task = new Task();
        task.setTitle("Разработка алгоритма СППР");
        task.setStatus("IN_PROGRESS");
        taskRepository.save(task);

        // 2. Вызываем метод репозитория
        List<Task> inProgressTasks = taskRepository.findByStatus("IN_PROGRESS");

        // 3. Проверяем, что задача нашлась
        assertFalse(inProgressTasks.isEmpty());
        assertEquals("Разработка алгоритма СППР", inProgressTasks.get(0).getTitle());
    }
}
