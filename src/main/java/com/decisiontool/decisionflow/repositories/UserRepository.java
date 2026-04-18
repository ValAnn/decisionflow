package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Поиск для авторизации
    Optional<User> findByUsername(String username);
    // Поиск для интеграции с Jira
    Optional<User> findByJiraId(String jiraId);

    boolean existsByUsername(String username);

    @Query(value = """
    SELECT u.* FROM users u
    JOIN tasks t ON t.assignee_id = u.id
    WHERE t.department_id = :deptId AND u.role_id = :roleId
    GROUP BY u.id
    ORDER BY COUNT(t.id) DESC
    LIMIT 3
    """, nativeQuery = true)
List<User> findTopUsersByDepartmentAndRole(@Param("deptId") Long deptId, @Param("roleId") Long roleId);
}