package com.decisiontool.decisionflow.dtos;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
@AllArgsConstructor
public class DashboardOverviewDTO {
    private MetricDTO totalTasks;
    private MetricDTO inProgressTasks;
    private MetricDTO doneTasks;
    private MetricDTO overdueTasks;
    private RisksDTO risks;
    private PriorityDistributionDTO priorities;
    @Data
    @Builder
    @AllArgsConstructor
    public static class MetricDTO {
        private long value;
        private double changePercent;
    }
    @Data
    @Builder
    @AllArgsConstructor
    public static class RisksDTO {
        private long overdue;
        private long dueSoon;
    }
    @Data
    @Builder
    @AllArgsConstructor
    public static class PriorityDistributionDTO {
        private long high;
        private long medium;
        private long low;
        private long other;
        private List<CriticalTaskDTO> criticalTasks;
    }
    @Data
    @Builder
    @AllArgsConstructor
    public static class CriticalTaskDTO {
        private Long id;
        private String title;
        private String status;
        private String priority;
        private String deadlineAt;
    }
}
