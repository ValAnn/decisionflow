package com.decisiontool.decisionflow.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter @Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_jira_id")
    private String externalJiraId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;   // TO_DO, IN_PROGRESS, DONE
    private String priority; // LOW, MEDIUM, HIGH

    @Column(name = "required_specialization")
    private String requiredSpecialization; // Для нашей логики подбора

    // Аналитик (Создатель)
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    // Разработчик (Исполнитель)
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    // Лид (Утверждающий)
    @ManyToOne
    @JoinColumn(name = "lead_id")
    private User lead;

    // Ссылка на департамент (создадим сущность Department позже или используем String)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "spent_hours")
    private Integer spentHours;
}