package com.decisiontool.decisionflow.dtos;

import lombok.Data;
import java.util.List;

@Data
public class CreateDeveloperDTO {
    private String fullName;
    private String username;
    private String email;
    private String grade;
    private Long specializationId; // Основная специализация из таблицы skills
    private List<Long> skillIds;   // Список дополнительных навыков для таблицы developer_skills
}