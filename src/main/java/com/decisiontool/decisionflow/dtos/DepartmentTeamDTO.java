package com.decisiontool.decisionflow.dtos;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@AllArgsConstructor
@Builder
public class DepartmentTeamDTO {
    private Long departmentId;
    private String departmentName;
    private List<UserStatsDTO> topAnalysts;
    private List<UserStatsDTO> topDevelopers;
}
