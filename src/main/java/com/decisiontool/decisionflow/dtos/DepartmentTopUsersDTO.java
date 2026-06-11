package com.decisiontool.decisionflow.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DepartmentTopUsersDTO {
    private Long departmentId;
    private String departmentName;
    private List<String> topAnalysts;    // Список фио аналитиков ["Иванов И.И.", ...]
    private List<String> topDevelopers;  // Список фио разработчиков ["Петров П.П.", ...]
}