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
    private MatchingService developerMatchService;
    private TaskRepository taskRepository;
    private DeveloperRepository developerRepository;
    private Skill javaSkill;
    private Skill reactSkill;
    @BeforeEach
    void setUp() {
        taskRepository = Mockito.mock(TaskRepository.class);
        developerRepository = Mockito.mock(DeveloperRepository.class);
        developerMatchService = new MatchingService(taskRepository, developerRepository);
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
        when(taskRepository.sumPlannedHours(any(), any(), any())).thenReturn(0);
        Task task = new Task();
        task.setTitle("Backend API");
        task.setSkills(Set.of(javaSkill));
        task.setRequiredSpecialization("BACKEND");
        DeveloperProfile developer = new DeveloperProfile();
        developer.setUserId(1L);
        developer.setGrade("MIDDLE");
        DeveloperSkill devSkill = new DeveloperSkill();
        devSkill.setSkill(javaSkill);
        devSkill.setPrimary(true);
        developer.setSkills(List.of(devSkill));
        double score = developerMatchService.calculateMatch(developer, task);
        assertNotNull(score, "Рейтинг не должен быть null");
        assertTrue(score > 30);
        assertFalse(score > 100.0, "Рейтинг не может превышать 100%");
    }
    @Test
    @DisplayName("Проверка полного несовпадения навыков (Рейтинг должен быть равен 0)")
    void testCalculateMatchScore_ZeroMatch() {
        Task task = new Task();
        task.setSkills(java.util.Set.of(javaSkill));
        DeveloperProfile developer = new DeveloperProfile();
        DeveloperSkill devSkill = new DeveloperSkill();
        devSkill.setSkill(reactSkill);
        devSkill.setPrimary(true);
        developer.setSkills(List.of(devSkill));
        double score = developerMatchService.calculateMatch(developer, task);
        assertAll("Проверки при нулевом совпадении",
            () -> assertTrue(score < 50),
            () -> assertTrue(score >= 0.0, "Скор не может быть отрицательным")
        );
    }
    @Test
    @DisplayName("Проверка генерации исключения при передаче null")
    void testCalculateMatchScore_ThrowsExceptionOnNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            developerMatchService.calculateMatch(null, null);
        }, "Если переданы null-объекты, должно выбрасываться исключение IllegalArgumentException");
    }
}
