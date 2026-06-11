package com.decisiontool.decisionflow.repositories;
import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByCreatorId(Long creatorId);
    List<Task> findAllByAssigneeId(Long assigneeId);
    List<Task> findAllByStatus(String status);
    List<Task> findByStatus(String status);
    Optional<Task> findByExternalJiraId(String jira_id);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :devId " +
           "AND t.status = 'К ВЫПОЛНЕНИЮ'")
    long countActiveTasksByDeveloperId(@Param("devId") Long devId);

    @Query(value = """
        SELECT d.name FROM departments d
        JOIN tasks t ON t.department_id = d.id
        WHERE t.assignee_id = :devId
        AND t.updated_at IS NOT NULL
        GROUP BY d.name
        ORDER BY COUNT(t.id) DESC LIMIT 3
    """, nativeQuery = true)
    List<String> findTopDepartmentsByDeveloperId(@Param("devId") Long devId);

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
       "FROM Task t WHERE t.department.id = :deptId AND t.assignee.id = :userId")
Double getCompletionPercentage(@Param("deptId") Long deptId, @Param("userId") Long userId);
    
@Query("SELECT SUM(t.spentHours) FROM Task t " +
       "WHERE t.assignee.id = :userId " +
       "AND t.deadlineAt BETWEEN :start AND :end")
Integer sumPlannedHours(@Param("userId") Long userId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

    List<Task> findAllByAnalystUsername(String username);
    boolean existsByExternalJiraId(String externalJiraId);

    // Выбираем только имена разработчиков
    @Query(value = "SELECT u.full_name FROM users u " +
            "JOIN tasks t ON t.assignee_id = u.id " +
            "WHERE t.department_id = :departmentId " +
            "GROUP BY u.id, u.full_name " +
            "ORDER BY COUNT(t.id) DESC " +
            "LIMIT 3", nativeQuery = true)
    List<String> findTopDeveloperNamesByDepartment(@Param("departmentId") Long departmentId);

    // Выбираем только имена аналитиков
    @Query(value = "SELECT u.full_name FROM users u " +
            "JOIN tasks t ON t.analyst_id = u.id " +
            "WHERE t.department_id = :departmentId " +
            "GROUP BY u.id, u.full_name " +
            "ORDER BY COUNT(t.id) DESC " +
            "LIMIT 3", nativeQuery = true)
    List<String> findTopAnalystNamesByDepartment(@Param("departmentId") Long departmentId);
}
