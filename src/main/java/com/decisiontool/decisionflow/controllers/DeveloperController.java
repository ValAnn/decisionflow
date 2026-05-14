package com.decisiontool.decisionflow.controllers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decisiontool.decisionflow.dtos.DeveloperProfileDTO;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.entities.Skill;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.DeveloperRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import com.decisiontool.decisionflow.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DeveloperRepository developerProfile;

    @GetMapping("/{id}/profile")
    public ResponseEntity<DeveloperProfileDTO> getFullInfo(@PathVariable("id") Long id) {
        // 1. Ищем пользователя (для имени)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Ищем его профиль (для специализации)
        DeveloperProfile profile = developerProfile.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Profile not found for this user"));

        // 3. Собираем агрегаты из задач
        long workload = taskRepository.countActiveTasksByDeveloperId(id);
        List<String> depts = taskRepository.findTopDepartmentsByDeveloperId(id);
        Double velocity = taskRepository.getAvgVelocityByDeveloperId(id);

        // 4. Собираем всё в DTO
        DeveloperProfileDTO fullInfo = new DeveloperProfileDTO(
            user.getId(),
            user.getFullName(),
            profile.getSpecialization()
                .map(Skill::getName)
                .orElse("Не указано"), 
            workload,
            depts,
            velocity != null ? Math.round(velocity * 10.0) / 10.0 : 0.0
        );

        return ResponseEntity.ok(fullInfo);
    }

    @GetMapping("/profile")
    public ResponseEntity<List<DeveloperProfileDTO>> getAllDevelopers() {
        // 1. Получаем всех пользователей и их профили
        List<User> users = userRepository.findAll();
        
        // 2. Предзагружаем агрегаты (для оптимизации в реальном проекте лучше использовать Map)
        List<DeveloperProfileDTO> dtos = users.stream().<DeveloperProfileDTO>map(user -> {
            // Пытаемся найти профиль для каждого пользователя
            DeveloperProfile profile = developerProfile.findByUserId(user.getId()).orElse(null);
            
            // Если профиля нет, мы можем либо пропустить пользователя, либо вернуть пустые поля
            if (profile == null) return null;

            // Собираем аналитику (пока оставим вызовы репозиториев, для диплома на небольших данных это ок)
            long workload = taskRepository.countActiveTasksByDeveloperId(user.getId());
            List<String> depts = taskRepository.findTopDepartmentsByDeveloperId(user.getId());
            Double velocity = taskRepository.getAvgVelocityByDeveloperId(user.getId());

            return DeveloperProfileDTO.builder()
                .id(user.getId())
                .name(user.getFullName())
                .specialization(profile.getSpecialization()
                    .map(Skill::getName)
                    .orElse("Не указано"))
                .currentWorkload(workload)
                .topDepartments(depts)
                .avgVelocityHours(velocity != null ? Math.round(velocity * 10.0) / 10.0 : 0.0)
                .build();
        })
        .filter(Objects::nonNull) // Убираем тех, у кого нет профиля
        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}