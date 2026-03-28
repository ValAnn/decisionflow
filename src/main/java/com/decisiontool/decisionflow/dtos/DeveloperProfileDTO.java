package com.decisiontool.decisionflow.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DeveloperProfileDTO {
    private Long id;
    private String name;
    private String specialization;
    private long currentWorkload;
    private List<String> topDepartments;
    private Double avgVelocityHours;
}
