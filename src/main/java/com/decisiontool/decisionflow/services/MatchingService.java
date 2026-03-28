package com.decisiontool.decisionflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RestTemplate restTemplate;
    // URL твоего запущенного FastAPI
    private final String PYTHON_SERVICE_URL = "http://localhost:8000/predict";

    public double predictMatchingScore(Long taskId, Long developerId) {
        // Формируем JSON-запрос для Python
        Map<String, Object> request = Map.of(
            "task_id", taskId,
            "developer_id", developerId
        );

        try {
            // Отправляем POST запрос и получаем ответ в виде Map
            Map<String, Object> response = restTemplate.postForObject(PYTHON_SERVICE_URL, request, Map.class);

            if (response != null && response.containsKey("match_percent")) {
                return (Double) response.get("match_percent");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при вызове Python ML сервиса: " + e.getMessage());
        }

        return 0.0; // Возвращаем 0, если сервис недоступен
    }
}