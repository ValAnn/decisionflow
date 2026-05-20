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
    // Метод полного обновления задачи в Jira на основе данных со страницы формы
    public void updateIssue(String issueKey, String title, String description, String priorityName, List<String> skills, String assigneeJiraId) {
        Map<String, Object> fields = new HashMap<>();
        
        // 1. Обновляем название задачи
        fields.put("summary", title);

        // 2. Обновляем описание (переводим обычный текст в формат ADF для API v3)
        if (description != null) {
            Map<String, Object> descriptionObj = Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(Map.of(
                    "type", "paragraph",
                    "content", List.of(Map.of(
                        "type", "text", 
                        "text", description
                    ))
                ))
            );
            fields.put("description", descriptionObj);
        }

        // 3. Обновляем приоритет (Jira ждет объект с именем приоритета, например: "High", "Medium", "Low")
        if (priorityName != null) {
            fields.put("priority", Map.of("name", mapPriorityToJira(priorityName)));
        }

        // 4. Обновляем навыки задачи (в Jira это стандартное поле меток - labels, принимает массив строк)
        if (skills != null) {
            fields.put("labels", skills);
        }

        // 5. Обновляем исполнителя (через accountId)
        if (assigneeJiraId != null && !assigneeJiraId.isEmpty()) {
            fields.put("assignee", Map.of("id", assigneeJiraId));
        } else {
            fields.put("assignee", null); // Если исполнитель не выбран / снят
        }

        // Формируем финальное тело запроса для PUT /rest/api/3/issue/{key}
        Map<String, Object> updateBody = Map.of("fields", fields);

        try {
            webClient.put()
                    .uri("/rest/api/3/issue/{key}", issueKey)
                    .bodyValue(updateBody)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            System.out.println("Задача " + issueKey + " успешно синхронизирована со всеми полями в Jira");
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении полей задачи в Jira: " + e.getMessage());
            // Для отказоустойчивости в дипломе ошибку можно залогировать, не ломая локальное сохранение в БД
        }
    }

    // Вспомогательный метод маппинга приоритетов на английские дефолтные имена Jira
    private String mapPriorityToJira(String priority) {
        return switch (priority.trim().toUpperCase()) {
            case "HIGH", "ВЫСОКИЙ", "CRITICAL" -> "High";
            case "LOW", "НИЗКИЙ" -> "Low";
            default -> "Medium"; // По умолчанию дефолтим на средний
        };
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
    "labels",   // Метки
    "priority",           
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

    
    public void updateIssueStatus(String jiraId, String newStatus) {
        // Формируем URL для конкретной задачи (например, .../issue/DEV-101/transitions)
        //String url =  jiraId + "/transitions";

        // Для Jira статус меняется через ID перехода (transition ID).
        // Ниже — упрощенное тело запроса. На защите это покажет, что ты понимаешь специфику Jira API
        Map<String, Object> requestBody = Map.of(
            "transition", Map.of("id", getTransitionIdByStatus(newStatus))
        );

        try {
            // Отправляем POST/PUT запрос в Jira
            webClient.post() // Рекомендуется использовать POST для JQL
            .uri("/rest/api/3/issue/{key}/transitions", jiraId)
            .bodyValue(requestBody)
            .retrieve()
                .toBodilessEntity()
                .block();
            System.out.println("Запрос в Jira отправлен: задача " + jiraId + " -> статус " + newStatus);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при обращении к Jira API: " + e.getMessage(), e);
        }
    }

    // Вспомогательный метод: маппинг твоих статусов на ID переходов в Jira
    private String getTransitionIdByStatus(String status) {
        return switch (status.trim().toUpperCase()) {
        case "TODO", "К ВЫПОЛНЕНИЮ", "OPEN" -> "11";
        case "IN_PROGRESS", "В РАБОТЕ"       -> "21";
        case "DONE", "COMPLETED", "ГОТОВО"   -> "31";
        case "RESEARCH", "ИССЛЕДОВАНИЕ"     -> "2";
        case "CREATED", "СОЗДАНО"            -> "3";
        default -> {
            // Если пришел какой-то специфичный статус, логируем и дефолтимся на "В работе" или кидаем ошибку
            //log.warn("Неизвестный статус для Jira: {}. Устанавливаем дефолтный переход 'В работе' (21)", status);
            yield "21"; 
        }
    };
    }
}