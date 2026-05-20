package com.decisiontool.decisionflow.dtos;

import lombok.Data;
import java.util.List;

@Data
public class TaskCreateDto {
    private String title;
    private String description;
    private String status;
    private String priority;
    private String requiredSpecialization;
    private Long departmentId;
    private String deadlineAt;
    private DeveloperRef developer;
    private List<SkillDto> skills; // Переименовали в skills

    @Data
    public static class DeveloperRef {
        private Long id;
    }

    @Data
    public static class SkillDto {
        private Long id;
        private String name;
    }
}