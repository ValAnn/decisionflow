package com.decisiontool.decisionflow.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserStatsDTO {
    private String fullName;
    private Double completionRate;
    // getters/setters
}