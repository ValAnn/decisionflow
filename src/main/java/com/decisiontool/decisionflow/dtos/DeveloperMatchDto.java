package com.decisiontool.decisionflow.dtos;
import java.util.List;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DeveloperMatchDto {
    private Long developerId;
    private String developerName;
    private String specialization;
    private String grade;
    private double matchPercent;
    private int plannedHoursNextTwoWeeks;
    private List<String> matchedSkills;
}
