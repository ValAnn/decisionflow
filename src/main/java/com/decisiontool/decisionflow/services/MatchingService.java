package com.decisiontool.decisionflow.services;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.repositories.DeveloperRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchingService {

    
    private final TaskRepository taskRepository;
    private final DeveloperRepository developerRepository;


    public double calculateMatch(DeveloperProfile profile, Task task) {
        // 1. Специализация (60%)
        double specScore = 0;
        // if (profile.getSpecialization() != null && 
        //     profile.getSpecialization().getName().equals(task.getRequiredSpecialization())) {
        //     specScore = 1.0;
        // }

        // 2. Свободные часы (20%)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoWeeksOut = now.plusDays(14);
        Integer busyHours = taskRepository.sumPlannedHours(profile.getUserId(), now, twoWeeksOut);
        if (busyHours == null) busyHours = 0;
        
        double loadScore = Math.max(0, (80.0 - busyHours) / 80.0);

        // 3. Грейд (10%)
        String grade = profile.getGrade();
        double gradeScore = 0; // Дефолтное значение, если грейд null или неизвестен
        
        if (grade != null) {
            gradeScore = switch (grade.trim().toUpperCase()) {
                case "SENIOR" -> 1.0;
                case "MIDDLE" -> 0.7;
                case "JUNIOR" -> 0.4;
                default -> 0.5;
            };
        }

        // 4. Дополнительные теги (10%)
        double tagsScore = 0.5; 

        double finalScore = (specScore * 0.6) + (loadScore * 0.2) + (gradeScore * 0.1) + (tagsScore * 0.1);
        
        return Math.min(100, Math.round(finalScore * 100.0));
    }

    public double predictMatchingScore(Long profileId, Long taskId){
        DeveloperProfile profile = developerRepository.findById(profileId).get();
        Task task = taskRepository.findById(taskId).get();

        return calculateMatch(profile,task);
    }
}
