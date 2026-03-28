package com.decisiontool.decisionflow.controllers;

import com.decisiontool.decisionflow.services.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * Эндпоинт для получения процента совместимости.
     * Пример вызова: GET /api/matching/predict?taskId=1&devId=5
     */
    @GetMapping("/predict")
public Map<String, Object> getPrediction(
        @RequestParam("taskId") Long taskId, 
        @RequestParam("devId") Long devId) {
        double score = matchingService.predictMatchingScore(taskId, devId);
        
        // Возвращаем JSON с результатом
        return Map.of(
            "taskId", taskId,
            "developerId", devId,
            "matchPercent", score,
            "message", "Оценка сформирована интеллектуальным модулем (прототип)"
        );
    }
}