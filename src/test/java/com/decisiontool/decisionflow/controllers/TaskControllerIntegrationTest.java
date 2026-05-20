package com.decisiontool.decisionflow.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ANALYST") // Имитируем авторизованного аналитика
    @DisplayName("Проверка получения списка задач через REST API")
    void testGetTasksEndpoint_Success() throws Exception {
        mockMvc.perform(get("/api/tasks")
               .contentType(MediaType.APPLICATION_JSON))
               
               // Множественные проверки HTTP-ответа
               .andExpect(status().isOk()) // 1. Проверяем статус 200 OK
               .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 2. Проверяем заголовок Content-Type
               
               // 3. Проверки структуры JSON через JsonPath
               .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0)))) // Массив пришел (пустой или с данными)
               .andExpect(jsonPath("$[0].id").exists()) // У первой задачи есть ID
               .andExpect(jsonPath("$[0].title", notNullValue())) // Заголовок задачи не пустой
               .andExpect(jsonPath("$[0].status", anyOf(is("OPEN"), is("IN_PROGRESS"), is("COMPLETED")))); // Статус валидный
    }
}