package com.decisiontool.decisionflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.decisiontool.decisionflow.services.JiraIntegrationService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;


@SpringBootTest
class DecisionflowApplicationTests {

    private static MockWebServer mockBackEnd;
    private JiraIntegrationService jiraService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        // Подменяем реальный URL Jira на адрес нашего локального Mock-сервера
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        WebClient.Builder webClientBuilder = WebClient.builder();
        
        // Передаем заглушки для username и token
        jiraService = new JiraIntegrationService(webClientBuilder, "test-user", "test-token");
        // ВАЖНО: В JiraIntegrationService поле jiraUrl должно быть доступно 
        // или передаваться через конструктор для тестов.
    }

    @Test
    void getIssue_shouldReturnTaskData() throws Exception {
        // 1. Готовим "липовый" ответ от Jira
        String mockJsonResponse = "{\"key\": \"KAN-1\", \"fields\": {\"summary\": \"Test Task\"}}";
        
        mockBackEnd.enqueue(new MockResponse()
                .setBody(mockJsonResponse)
                .addHeader("Content-Type", "application/json"));

        // 2. Вызываем наш метод
        Map<String, Object> result = jiraService.getIssue("KAN-1");

        // 3. Проверяем результаты
        assertNotNull(result);
        assertEquals("KAN-1", result.get("key"));
        
        // Проверяем, что запрос ушел на правильный URL
        var recordedRequest = mockBackEnd.takeRequest();
        assertEquals("/rest/api/3/issue/KAN-1", recordedRequest.getPath());
    }
}