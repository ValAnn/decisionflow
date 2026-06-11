package com.decisiontool.decisionflow.services;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import com.decisiontool.decisionflow.dtos.DashboardOverviewDTO;
import com.decisiontool.decisionflow.dtos.WorkloadPointDTO;
import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final Set<String> IN_PROGRESS_STATUSES = Set.of(
        "RESEARCH", "ИССЛЕДОВАНИЕ"
    );
    private static final Set<String> DONE_STATUSES = Set.of(
        "TO_DO"
    );
    private static final Set<String> TODO_STATUSES = Set.of(
        "СОЗДАНО", "CREATED"
    );
    private final TaskRepository taskRepository;
    public DashboardOverviewDTO getOverview(String username) {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = taskRepository.findAllByAnalystUsername(username);
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate prevMonthStart = monthStart.minusMonths(1);
        DashboardOverviewDTO.MetricDTO totalTasks = buildMetric(
            tasks,
            t -> true,
            monthStart,
            prevMonthStart
        );
        DashboardOverviewDTO.MetricDTO inProgress = buildMetric(
            tasks,
            t -> IN_PROGRESS_STATUSES.contains(normalizeStatus(t.getStatus())),
            monthStart,
            prevMonthStart
        );
        DashboardOverviewDTO.MetricDTO done = buildMetric(
            tasks,
            t -> DONE_STATUSES.contains(normalizeStatus(t.getStatus())),
            monthStart,
            prevMonthStart
        );
        DashboardOverviewDTO.MetricDTO overdue = buildMetric(
            tasks,
            t -> isOverdue(t, now),
            monthStart,
            prevMonthStart
        );
        long overdueCount = tasks.stream().filter(t -> isOverdue(t, now)).count();
        long dueSoonCount = tasks.stream().filter(t -> isDueSoon(t, now)).count();
        long high = tasks.stream().filter(t -> "HIGH".equals(normalizePriority(t.getPriority()))).count();
        long medium = tasks.stream().filter(t -> "MEDIUM".equals(normalizePriority(t.getPriority()))).count();
        long low = tasks.stream().filter(t -> "LOW".equals(normalizePriority(t.getPriority()))).count();
        long other = tasks.size() - high - medium - low;
        List<DashboardOverviewDTO.CriticalTaskDTO> criticalTasks = tasks.stream()
            .filter(t -> "HIGH".equals(normalizePriority(t.getPriority())))
            .filter(t -> !DONE_STATUSES.contains(normalizeStatus(t.getStatus())))
            .sorted(Comparator.comparing(Task::getDeadlineAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .limit(5)
            .map(t -> DashboardOverviewDTO.CriticalTaskDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .status(t.getStatus())
                .priority(t.getPriority())
                .deadlineAt(t.getDeadlineAt() != null ? t.getDeadlineAt().toString() : null)
                .build())
            .toList();
        return DashboardOverviewDTO.builder()
            .totalTasks(totalTasks)
            .inProgressTasks(inProgress)
            .doneTasks(done)
            .overdueTasks(overdue)
            .risks(DashboardOverviewDTO.RisksDTO.builder()
                .overdue(overdueCount)
                .dueSoon(dueSoonCount)
                .build())
            .priorities(DashboardOverviewDTO.PriorityDistributionDTO.builder()
                .high(high)
                .medium(medium)
                .low(low)
                .other(other)
                .criticalTasks(criticalTasks)
                .build())
            .build();
    }
    public List<WorkloadPointDTO> getWorkload(String username, String period) {
        List<Task> tasks = taskRepository.findAllByAnalystUsername(username);
        String normalizedPeriod = period == null ? "week" : period.trim().toLowerCase(Locale.ROOT);
        return "month".equals(normalizedPeriod) ? buildMonthSeries(tasks) : buildWeekSeries(tasks);
    }
    private List<WorkloadPointDTO> buildWeekSeries(List<Task> tasks) {
        Map<DayOfWeek, Long> counts = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        for (Task task : tasks) {
            if (task.getCreatedAt() == null) {
                continue;
            }
            LocalDate day = task.getCreatedAt().toLocalDate();
            if (!day.isBefore(start) && !day.isAfter(today)) {
                counts.put(day.getDayOfWeek(), counts.getOrDefault(day.getDayOfWeek(), 0L) + 1);
            }
        }
        List<WorkloadPointDTO> result = new ArrayList<>();
        DayOfWeek weekStart = start.getDayOfWeek();
        for (int i = 0; i < 7; i++) {
            DayOfWeek d = weekStart.plus(i);
            String label = d.getDisplayName(TextStyle.SHORT, new Locale("ru", "RU")).toUpperCase(Locale.ROOT);
            result.add(WorkloadPointDTO.builder().label(label).taskCount(counts.getOrDefault(d, 0L)).build());
        }
        return result;
    }
    private List<WorkloadPointDTO> buildMonthSeries(List<Task> tasks) {
        Map<Integer, Long> counts = new HashMap<>();
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(29);
        for (Task task : tasks) {
            if (task.getCreatedAt() == null) {
                continue;
            }
            LocalDate day = task.getCreatedAt().toLocalDate();
            if (!day.isBefore(start) && !day.isAfter(now)) {
                counts.put(day.getDayOfMonth(), counts.getOrDefault(day.getDayOfMonth(), 0L) + 1);
            }
        }
        List<WorkloadPointDTO> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate day = start.plusDays(i);
            result.add(WorkloadPointDTO.builder()
                .label(day.toString())
                .taskCount(counts.getOrDefault(day.getDayOfMonth(), 0L))
                .build());
        }
        return result;
    }
    private DashboardOverviewDTO.MetricDTO buildMetric(
        List<Task> tasks,
        Predicate<Task> filter,
        LocalDate currentStart,
        LocalDate prevStart
    ) {
        long total = tasks.stream().filter(filter).count();
        long current = tasks.stream()
            .filter(filter)
            .filter(t -> t.getCreatedAt() != null)
            .filter(t -> !t.getCreatedAt().toLocalDate().isBefore(currentStart))
            .count();
        long previous = tasks.stream()
            .filter(filter)
            .filter(t -> t.getCreatedAt() != null)
            .filter(t -> {
                LocalDate date = t.getCreatedAt().toLocalDate();
                return !date.isBefore(prevStart) && date.isBefore(currentStart);
            })
            .count();
        return DashboardOverviewDTO.MetricDTO.builder()
            .value(total)
            .changePercent(calculatePercentChange(current, previous))
            .build();
    }
    private double calculatePercentChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round(((current - previous) * 1000.0 / previous)) / 10.0;
    }
    private boolean isOverdue(Task task, LocalDateTime now) {
        return task.getDeadlineAt() != null
            && task.getDeadlineAt().isBefore(now)
            && !DONE_STATUSES.contains(normalizeStatus(task.getStatus()));
    }
    private boolean isDueSoon(Task task, LocalDateTime now) {
        return task.getDeadlineAt() != null
            && (task.getDeadlineAt().isEqual(now) || task.getDeadlineAt().isAfter(now))
            && task.getDeadlineAt().isBefore(now.plusDays(7))
            && !DONE_STATUSES.contains(normalizeStatus(task.getStatus()));
    }
    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (IN_PROGRESS_STATUSES.contains(normalized)) {
            return "IN_PROGRESS";
        }
        if (DONE_STATUSES.contains(normalized)) {
            return "DONE";
        }
        if (TODO_STATUSES.contains(normalized)) {
            return "TO_DO";
        }
        return normalized;
    }
    private String normalizePriority(String priority) {
        if (priority == null) {
            return "OTHER";
        }
        String normalized = priority.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "HIGH", "ВЫСОКИЙ", "CRITICAL" -> "HIGH";
            case "MEDIUM", "СРЕДНИЙ" -> "MEDIUM";
            case "LOW", "НИЗКИЙ" -> "LOW";
            default -> "OTHER";
        };
    }
}
