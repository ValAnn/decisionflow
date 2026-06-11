package com.decisiontool.decisionflow.services;
import com.decisiontool.decisionflow.dtos.DeveloperMatchDto;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.entities.DeveloperSkill;
import com.decisiontool.decisionflow.entities.Skill;
import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.repositories.DeveloperRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final TaskRepository taskRepository;
    private final DeveloperRepository developerRepository;
    public double calculateMatch(DeveloperProfile profile, Task task) {
        if (profile == null || task == null) {
            throw new IllegalArgumentException("Developer profile and task are required");
        }
        double specializationScore = calculateSpecializationScore(profile, task);
        double skillScore = calculateSkillScore(profile, task);
        double loadScore = calculateLoadScore(profile);
        double gradeScore = calculateGradeScore(profile.getGrade());
        double finalScore = (specializationScore * 0.35)
                + (skillScore * 0.35)
                + (loadScore * 0.20)
                + (gradeScore * 0.10);
        return Math.min(100, Math.round(finalScore * 100.0));
    }
    public double predictMatchingScore(Long taskId, Long profileId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        DeveloperProfile profile = developerRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Developer profile not found: " + profileId));
        return calculateMatch(profile, task);
    }
    public List<DeveloperMatchDto> recommendDevelopers(Long taskId, int limit) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        int resultLimit = limit > 0 ? limit : 5;
        return developerRepository.findAll().stream()
                .map(profile -> toRecommendation(profile, task))
                .sorted(Comparator.comparingDouble(DeveloperMatchDto::getMatchPercent).reversed())
                .limit(resultLimit)
                .collect(Collectors.toList());
    }
    private DeveloperMatchDto toRecommendation(DeveloperProfile profile, Task task) {
        return DeveloperMatchDto.builder()
                .developerId(profile.getUserId())
                .developerName(profile.getUser() != null ? profile.getUser().getFullName() : null)
                .specialization(profile.getSpecialization().map(Skill::getName).orElse(null))
                .grade(profile.getGrade())
                .matchPercent(calculateMatch(profile, task))
                .plannedHoursNextTwoWeeks(getBusyHours(profile))
                .matchedSkills(getMatchedSkills(profile, task))
                .build();
    }
    private double calculateSpecializationScore(DeveloperProfile profile, Task task) {
        String requiredSpecialization = task.getRequiredSpecialization();
        if (requiredSpecialization == null) {
            return taskSkillNames(task).isEmpty() ? 0.5 : 0.0;
        }
        Set<String> developerSkills = developerSkillNames(profile);
        boolean matchesAnySkill = developerSkills.contains(requiredSpecialization);
        boolean matchesPrimarySkill = profile.getSpecialization()
                .map(Skill::getCategory)
                // .map(this::normalize)
                .filter(requiredSpecialization::equals)
                .isPresent();
        if (matchesPrimarySkill) {
            return 1.0;
        }
        return matchesAnySkill ? 0.8 : 0.0;
    }
    private double calculateSkillScore(DeveloperProfile profile, Task task) {
        Set<String> taskSkills = taskSkillNames(task);
        if (taskSkills.isEmpty()) {
            return 0.5;
        }
        Set<String> developerSkills = developerSkillNames(profile);
        long matches = taskSkills.stream()
                .filter(developerSkills::contains)
                .count();
        return matches / (double) taskSkills.size();
    }
    private double calculateLoadScore(DeveloperProfile profile) {
        int busyHours = getBusyHours(profile);
        return Math.max(0, (80.0 - busyHours) / 80.0);
    }
    private int getBusyHours(DeveloperProfile profile) {
        if (profile.getUserId() == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        Integer busyHours = taskRepository.sumPlannedHours(profile.getUserId(), now, now.plusDays(14));
        return busyHours != null ? busyHours : 0;
    }
    private double calculateGradeScore(String grade) {
        if (grade == null) {
            return 0.5;
        }
        return switch (grade.trim().toUpperCase(Locale.ROOT)) {
            case "SENIOR" -> 1.0;
            case "MIDDLE" -> 0.7;
            case "JUNIOR" -> 0.4;
            default -> 0.5;
        };
    }
    private List<String> getMatchedSkills(DeveloperProfile profile, Task task) {
        Set<String> developerSkills = developerSkillNames(profile);
        return task.getSkills().stream()
                .map(Skill::getName)
                .filter(Objects::nonNull)
                .filter(skill -> developerSkills.contains(normalize(skill)))
                .collect(Collectors.toList());
    }
    private Set<String> developerSkillNames(DeveloperProfile profile) {
        return profile.getSkills().stream()
                .map(DeveloperSkill::getSkill)
                .filter(Objects::nonNull)
                .map(Skill::getName)
                // .map(this::normalize)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    private Set<String> taskSkillNames(Task task) {
        return task.getSkills().stream()
                .map(Skill::getName)
            //    .map(this::normalize)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replace('_', ' ').toLowerCase(Locale.ROOT);
    }
}
