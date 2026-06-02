package com.decisiontool.decisionflow.controllers;

import com.decisiontool.decisionflow.dtos.DeveloperMatchDto;
import com.decisiontool.decisionflow.services.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/predict")
    public Map<String, Object> getPrediction(
            @RequestParam("taskId") Long taskId,
            @RequestParam("devId") Long devId) {
        double score = matchingService.predictMatchingScore(taskId, devId);

        return Map.of(
                "taskId", taskId,
                "developerId", devId,
                "matchPercent", score,
                "message", "Match score calculated"
        );
    }

    @GetMapping("/recommendations")
    public List<DeveloperMatchDto> getRecommendations(
            @RequestParam("taskId") Long taskId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return matchingService.recommendDevelopers(taskId, limit);
    }
}
