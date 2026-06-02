package com.decisiontool.decisionflow.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WorkloadPointDTO {
    private String label;
    private long taskCount;
}
