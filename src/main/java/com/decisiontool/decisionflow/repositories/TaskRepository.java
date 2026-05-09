package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Задачи, созданные конкретным аналитиком
    List<Task> findAllByCreatorId(Long creatorId);
    // Задачи, назначенные разработчику
    List<Task> findAllByAssigneeId(Long assigneeId);
    // Список задач по статусу (например, "TODO" или "IN_PROGRESS")
    List<Task> findAllByStatus(String status);

    List<Task> findByExternalJiraId(String jira_id);

    // 1. Активные задачи (те, что в процессе И еще не завершены)
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :devId " +
           "AND t.status = 'IN_PROGRESS' AND t.updatedAt IS NULL")
    long countActiveTasksByDeveloperId(@Param("devId") Long devId);

    // 2. ТОП-3 департамента (только по реально ЗАВЕРШЕННЫМ задачам)
    @Query(value = """
        SELECT d.name FROM departments d 
        JOIN tasks t ON t.department_id = d.id 
        WHERE t.assignee_id = :devId 
        AND t.updated_at IS NOT NULL 
        GROUP BY d.name 
        ORDER BY COUNT(t.id) DESC LIMIT 3
    """, nativeQuery = true)
    List<String> findTopDepartmentsByDeveloperId(@Param("devId") Long devId);

    // 3. Средняя скорость (только там, где есть обе даты для расчета)
    @Query(value = """
        SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600) 
        FROM tasks 
        WHERE assignee_id = :devId 
        AND updated_at IS NOT NULL
    """, nativeQuery = true)
    Double getAvgVelocityByDeveloperId(@Param("devId") Long devId);

    @Query("SELECT t.assignee.id, COUNT(t) FROM Task t WHERE t.completedAt IS NOT NULL GROUP BY t.assignee.id")
    List<Object[]> countAllActiveTasks();

    @Query("SELECT t FROM Task t WHERE t.assignee.username = :username")
    List<Task> findAllByAssigneeUsername(@Param("username") String username);

    @Query("SELECT t FROM Task t WHERE t.creator.username = :username")
    List<Task> findAllByCreatorUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN 0.0 ELSE " +
       "(SUM(CASE WHEN t.status = 'DONE' THEN 1.0 ELSE 0.0 END) * 100.0 / COUNT(t)) END " +
       "FROM Task t WHERE t.departmentId = :deptId AND t.assignee.id = :userId")
Double getCompletionPercentage(@Param("deptId") Long deptId, @Param("userId") Long userId);

    @Query("SELECT SUM(t.spentHours) FROM Task t " +
       "WHERE t.assignee.id = :userId " +
       "AND t.deadlineAt BETWEEN :start AND :end")
Integer sumPlannedHours(@Param("userId") Long userId, 
                        @Param("start") LocalDateTime start, 
                        @Param("end") LocalDateTime end);
    
    List<Task> findAllByAnalystUsername(String username);

    // Проверка, чтобы не дублировать задачи при повторном импорте
    boolean existsByExternalJiraId(String externalJiraId);
}