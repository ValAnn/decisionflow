package com.decisiontool.decisionflow.services;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static com.decisiontool.decisionflow.config.JiraConstants.TRANSITION_TO_DO;



@Service
public class JiraIntegrationService {

    private String jiraUrl;

    private final WebClient webClient;

    public JiraIntegrationService(WebClient.Builder webClientBuilder, 
                                  @Value("${jira.base-url}") String url, // Добавляем сюда!
                                  @Value("${jira.username}") String user, 
                                  @Value("${jira.api-token}") String token) {
        this.jiraUrl = url;
        this.webClient = webClientBuilder
                .baseUrl(url) // Теперь здесь точно будет значение из конфига
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + 
                    Base64.getEncoder().encodeToString((user + ":" + token).getBytes()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    // Получить данные задачи из Jira
    public Map<String, Object> getIssue(String issueKey) {
        System.out.println(webClient.get()
                .uri("/rest/api/3/issue/{key}", issueKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block());
        return webClient.get()
                .uri("/rest/api/3/issue/{key}", issueKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // Отправить ТЗ и назначить человека в Jira
    public void updateIssue(String issueKey, String description, String assigneeId) {
        Map<String, Object> updateBody = Map.of(
            "fields", Map.of(
                "description", description, // В Jira v3 это Document Format, для простоты берем v2 или plain text
                "assignee", Map.of("id", assigneeId)
            )
        );

        webClient.put()
                .uri("/rest/api/3/issue/{key}", issueKey)
                .bodyValue(updateBody)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    // JiraIntegrationService.java
public void moveToTodo(String issueKey, String jiraAccountId) {
    // 1. Назначаем исполнителя через accountId
    // Эндпоинт: PUT /rest/api/3/issue/{issueKey}/assignee
    Map<String, String> assigneeBody = Map.of("accountId", jiraAccountId);

    webClient.put()
            .uri("/rest/api/3/issue/{key}/assignee", issueKey)
            .bodyValue(assigneeBody)
            .retrieve()
            .toBodilessEntity()
            .doOnError(e -> System.err.println("Ошибка назначения: " + e.getMessage()))
            .block();

    // 2. Переводим задачу в статус "To Do"
    // Эндпоинт: POST /rest/api/3/issue/{issueKey}/transitions
    Map<String, Object> transitionBody = Map.of(
        "transition", Map.of("id", TRANSITION_TO_DO) // ID перехода для твоего проекта
    );

    webClient.post()
            .uri("/rest/api/3/issue/{key}/transitions", issueKey)
            .bodyValue(transitionBody)
            .retrieve()
            .toBodilessEntity()
            .block();
}

    // JiraIntegrationService.java

public List<Map<String, Object>> getIssuesForAnalyst(String analystAccountId) {
    // JQL остается прежним
    String jql = String.format("cf[10071] = \"%s\" ",  //AND assignee is EMPTY AND status in (\"Исследование\", \"Создано\") cf[10071] = \"712020:b797b769-6517-4e24-8750-993ff6942d11\" 
                               analystAccountId);

    // Создаем тело запроса для метода POST /rest/api/3/search/jql
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("jql", jql);
    requestBody.put("maxResults", 50); // Можно настроить по необходимости
    requestBody.put("fields", List.of(
    "summary", 
    "status", 
    "description", 
    "labels",              // Метки
    "customfield_10071",   // Analyst
    "customfield_10072",   // Департамент
    "customfield_10073"    // Специализация
));
    // В новом API для пагинации используется nextPageToken, а не startAt

    Map<String, Object> response = webClient.post() // Рекомендуется использовать POST для JQL
            .uri("/rest/api/3/search/jql")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

    if (response == null || !response.containsKey("issues")) {
        return Collections.emptyList();
    }

    // В ответе теперь также приходит поле "isLast" и "nextPageToken" для следующего шага
    return (List<Map<String, Object>>) response.get("issues");
}

    
}