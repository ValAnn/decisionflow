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
                                  @Value("${jira.base-url}") String url,
                                  @Value("${jira.username}") String user,
                                  @Value("${jira.api-token}") String token) {
        this.jiraUrl = url;
        this.webClient = webClientBuilder
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                    Base64.getEncoder().encodeToString((user + ":" + token).getBytes()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
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
    public void updateIssue(String issueKey, String title, String description, String priorityName, List<String> skills, String assigneeJiraId) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("summary", title);
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
        if (priorityName != null) {
            fields.put("priority", Map.of("name", mapPriorityToJira(priorityName)));
        }
        if (skills != null) {
            fields.put("labels", skills);
        }
        if (assigneeJiraId != null && !assigneeJiraId.isEmpty()) {
            fields.put("assignee", Map.of("id", assigneeJiraId));
        } else {
            fields.put("assignee", null);
        }
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
        }
    }
    private String mapPriorityToJira(String priority) {
        return switch (priority.trim().toUpperCase()) {
            case "HIGH", "ВЫСОКИЙ", "CRITICAL" -> "High";
            case "LOW", "НИЗКИЙ" -> "Low";
            default -> "Medium";
        };
    }
public void moveToTodo(String issueKey, String jiraAccountId) {
    Map<String, String> assigneeBody = Map.of("accountId", jiraAccountId);
    webClient.put()
            .uri("/rest/api/3/issue/{key}/assignee", issueKey)
            .bodyValue(assigneeBody)
            .retrieve()
            .toBodilessEntity()
            .doOnError(e -> System.err.println("Ошибка назначения: " + e.getMessage()))
            .block();
    Map<String, Object> transitionBody = Map.of(
        "transition", Map.of("id", TRANSITION_TO_DO)
    );
    webClient.post()
            .uri("/rest/api/3/issue/{key}/transitions", issueKey)
            .bodyValue(transitionBody)
            .retrieve()
            .toBodilessEntity()
            .block();
}
public List<Map<String, Object>> getIssuesForAnalyst(String analystAccountId) {
    String jql = String.format("cf[10071] = \"%s\" ",
                               analystAccountId);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("jql", jql);
    requestBody.put("maxResults", 50);
    requestBody.put("fields", List.of(
    "summary",
    "status",
    "description",
    "labels",
    "priority",
    "customfield_10071",
    "customfield_10072",
    "customfield_10073"
));
    Map<String, Object> response = webClient.post()
            .uri("/rest/api/3/search/jql")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();
    if (response == null || !response.containsKey("issues")) {
        return Collections.emptyList();
    }
    return (List<Map<String, Object>>) response.get("issues");
}
    public void updateIssueStatus(String jiraId, String newStatus) {
        Map<String, Object> requestBody = Map.of(
            "transition", Map.of("id", getTransitionIdByStatus(newStatus))
        );
        try {
            webClient.post()
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
    private String getTransitionIdByStatus(String status) {
        return switch (status.trim().toUpperCase()) {
        case "TODO", "К ВЫПОЛНЕНИЮ", "OPEN" -> "11";
        case "IN_PROGRESS", "В РАБОТЕ"       -> "21";
        case "DONE", "COMPLETED", "ГОТОВО"   -> "31";
        case "RESEARCH", "ИССЛЕДОВАНИЕ"     -> "2";
        case "CREATED", "СОЗДАНО"            -> "3";
        default -> {
            yield "21";
        }
    };
    }
}
