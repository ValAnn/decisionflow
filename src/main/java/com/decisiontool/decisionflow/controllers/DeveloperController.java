package com.decisiontool.decisionflow.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.decisiontool.decisionflow.dtos.DeveloperProfileDTO;
import com.decisiontool.decisionflow.dtos.CreateDeveloperDTO;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.entities.DeveloperSkill;
import com.decisiontool.decisionflow.entities.Skill;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.DeveloperRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import com.decisiontool.decisionflow.repositories.UserRepository;
import com.decisiontool.decisionflow.repositories.SkillRepository; // Предполагаем наличие

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DeveloperRepository developerProfileRepository;
    private final SkillRepository skillRepository; // Добавили для поиска навыков

    @GetMapping("/{id}/profile")
    public ResponseEntity<DeveloperProfileDTO> getFullInfo(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DeveloperProfile profile = developerProfileRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Profile not found for this user"));
        
        long workload = taskRepository.countActiveTasksByDeveloperId(id);
        List<String> depts = taskRepository.findTopDepartmentsByDeveloperId(id);
        Double velocity = taskRepository.getAvgVelocityByDeveloperId(id);

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
        List<User> users = userRepository.findAll();
        List<DeveloperProfileDTO> dtos = users.stream().<DeveloperProfileDTO>map(user -> {
            DeveloperProfile profile = developerProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile == null) return null;

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
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Создание нового разработчика аналитиком (с поддержкой пустых профилей)
     */
    @PostMapping
    @Transactional
    public ResponseEntity<String> createDeveloper(@RequestBody CreateDeveloperDTO dto) {
        // 1. Создаем пользователя
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash("$2a$10$wZ7Xg..."); // Заглушка безопасности
        User savedUser = userRepository.save(user);

        // 2. Создаем профиль
        DeveloperProfile profile = new DeveloperProfile();
        profile.setUser(savedUser);
        profile.setGrade(dto.getGrade());
        profile.setSkills(new ArrayList<>()); 

        // 3. Безопасно обрабатываем основную специализацию (без orElseThrow)
        if (dto.getSpecializationId() != null && dto.getSpecializationId() != 0) {
            skillRepository.findById(dto.getSpecializationId()).ifPresent(specSkill -> {
                DeveloperSkill primarySkill = new DeveloperSkill();
                primarySkill.setProfile(profile);
                primarySkill.setSkill(specSkill);
                primarySkill.setPrimary(true);
                profile.getSkills().add(primarySkill);
            });
        }

        // 4. Безопасно обрабатываем дополнительные навыки
        if (dto.getSkillIds() != null && !dto.getSkillIds().isEmpty()) {
            // Фильтруем список от 0 и ищем только существующие в базе навыки
            List<Long> cleanIds = dto.getSkillIds().stream()
                    .filter(id -> id != 0 && !id.equals(dto.getSpecializationId()))
                    .collect(Collectors.toList());

            if (!cleanIds.isEmpty()) {
                List<Skill> additionalSkills = skillRepository.findAllById(cleanIds);
                for (Skill skill : additionalSkills) {
                    DeveloperSkill devSkill = new DeveloperSkill();
                    devSkill.setProfile(profile);
                    devSkill.setSkill(skill);
                    devSkill.setPrimary(false);
                    profile.getSkills().add(devSkill);
                }
            }
        }

        developerProfileRepository.save(profile);
        return ResponseEntity.ok("Разработчик успешно добавлен в систему");
    }

    /**
     * Редактирование существующего разработчика аналитиком (с поддержкой пустых профилей)
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<String> updateDeveloper(@PathVariable("id") Long id, @RequestBody CreateDeveloperDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DeveloperProfile profile = developerProfileRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Developer profile not found"));

        // Обновляем базовые данные пользователя
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        userRepository.save(user);

        // Обновляем грейд
        profile.setGrade(dto.getGrade());

        // Очищаем старые связи, чтобы перезаписать их без конфликтов
        profile.getSkills().clear();

        // 1. Пытаемся добавить новую основную специализацию
        if (dto.getSpecializationId() != null && dto.getSpecializationId() != 0) {
            skillRepository.findById(dto.getSpecializationId()).ifPresent(specSkill -> {
                DeveloperSkill primarySkill = new DeveloperSkill();
                primarySkill.setProfile(profile);
                primarySkill.setSkill(specSkill);
                primarySkill.setPrimary(true);
                profile.getSkills().add(primarySkill);
            });
        }

        // 2. Пытаемся добавить дополнительные навыки
        if (dto.getSkillIds() != null && !dto.getSkillIds().isEmpty()) {
            List<Long> cleanIds = dto.getSkillIds().stream()
                    .filter(skillId -> skillId != 0 && !skillId.equals(dto.getSpecializationId()))
                    .collect(Collectors.toList());

            if (!cleanIds.isEmpty()) {
                List<Skill> updatedAdditionalSkills = skillRepository.findAllById(cleanIds);
                for (Skill skill : updatedAdditionalSkills) {
                    DeveloperSkill devSkill = new DeveloperSkill();
                    devSkill.setProfile(profile);
                    devSkill.setSkill(skill);
                    devSkill.setPrimary(false);
                    profile.getSkills().add(devSkill);
                }
            }
        }

        developerProfileRepository.save(profile);
        return ResponseEntity.ok("Данные разработчика успешно обновлены");
    }
}