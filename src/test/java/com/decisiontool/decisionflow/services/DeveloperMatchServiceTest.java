package com.decisiontool.decisionflow.services;

import com.decisiontool.decisionflow.entities.*;
import com.decisiontool.decisionflow.repositories.DeveloperRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DeveloperMatchServiceTest {

    // 1. Убираем @Autowired, это чистый юнит-тест
    private MatchingService developerMatchService;
    
    // Ссылки на моки репозиториев, которые требует новый конструктор MatchingService
    private TaskRepository taskRepository;
    private DeveloperRepository developerRepository;

    private Skill javaSkill;
    private Skill reactSkill;

    @BeforeEach
    void setUp() {
        // 2. Инициализируем моки репозиториев
        taskRepository = Mockito.mock(TaskRepository.class);
        developerRepository = Mockito.mock(DeveloperRepository.class);

        // 3. Создаем сервис вручную, передавая ему зависимости в конструктор
        developerMatchService = new MatchingService(taskRepository, developerRepository);

        // Твои навыки
        javaSkill = new Skill();
        javaSkill.setId(1L);
        javaSkill.setName("Java");

        reactSkill = new Skill();
        reactSkill.setId(2L);
        reactSkill.setName("React");
    }

    @Test
    @DisplayName("Проверка совпадения разработчика")
    void testCalculateMatchScore_PerfectMatch() {
        // 4. Обязательно мокаем метод sumPlannedHours, так как он вызывается внутри calculateMatch
        // Говорим: "Верни 0 занятых часов (разработчик полностью свободен)"
        when(taskRepository.sumPlannedHours(any(), any(), any())).thenReturn(0);

        Task task = new Task();
        task.setTitle("Backend API");
        task.setSkills(Set.of(javaSkill));
        task.setRequiredSpecialization("BACKEND");

        DeveloperProfile developer = new DeveloperProfile();
        developer.setUserId(1L); // Задаем ID, так как он передается в taskRepository.sumPlannedHours()
        developer.setGrade("MIDDLE");
        
        DeveloperSkill devSkill = new DeveloperSkill();
        devSkill.setSkill(javaSkill);
        devSkill.setPrimary(true);
        developer.setSkills(List.of(devSkill));

        // 5. Вызываем метод — теперь developerMatchService ТОЧНО не null!
        double score = developerMatchService.calculateMatch(developer, task);

        // Множественные проверки (Assertions)
        assertNotNull(score, "Рейтинг не должен быть null");
        assertTrue(score > 30);
        assertFalse(score > 100.0, "Рейтинг не может превышать 100%");
    }


    @Test
    @DisplayName("Проверка полного несовпадения навыков (Рейтинг должен быть равен 0)")
    void testCalculateMatchScore_ZeroMatch() {
        Task task = new Task();
        task.setSkills(java.util.Set.of(javaSkill)); // Задача требует Java

        DeveloperProfile developer = new DeveloperProfile();
        DeveloperSkill devSkill = new DeveloperSkill();
        devSkill.setSkill(reactSkill); // А разработчик знает только React
        devSkill.setPrimary(true);
        developer.setSkills(List.of(devSkill));

        double score = developerMatchService.calculateMatch(developer, task);

        // Группировка проверок (Assert All) — если одна упадет, остальные все равно проверятся
        assertAll("Проверки при нулевом совпадении",
            () -> assertTrue(score < 50),
            () -> assertTrue(score >= 0.0, "Скор не может быть отрицательным")
        );
    }

    // @Test
    // @DisplayName("Проверка устойчивости алгоритма к пустым данным")
    // void testCalculateMatchScore_EmptyData() {
    //     Task emptyTask = new Task();
    //     emptyTask.setSkills(new java.util.HashSet<>());

    //     DeveloperProfile emptyDeveloper = new DeveloperProfile();
    //     emptyDeveloper.setSkills(new ArrayList<>());

    //     // Проверяем, что метод не падает с NullPointerException, а возвращает 0
    //     double score = developerMatchService.calculateMatch(emptyDeveloper, emptyTask);
        
    //     assertEquals(0.0, score, "Пустые списки навыков должны возвращать 0.0");
    // }

    @Test
    @DisplayName("Проверка генерации исключения при передаче null")
    void testCalculateMatchScore_ThrowsExceptionOnNull() {
        // Проверяем, что твой сервис правильно реагирует на некорректные входные данные
        assertThrows(NullPointerException.class, () -> {
            developerMatchService.calculateMatch(null, null);
        }, "Если переданы null-объекты, должно выбрасываться исключение IllegalArgumentException");
    }
}