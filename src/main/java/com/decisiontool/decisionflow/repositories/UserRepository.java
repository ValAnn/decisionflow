package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Поиск для авторизации
    Optional<User> findByUsername(String username);
    // Поиск для интеграции с Jira
    Optional<User> findByJiraId(String jiraId);

    boolean existsByUsername(String username);
}