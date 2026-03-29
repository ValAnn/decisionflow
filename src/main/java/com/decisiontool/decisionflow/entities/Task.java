package com.decisiontool.decisionflow.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @ManyToOne
    @JoinColumn(name = "developer_id")
    private User developer;

    // Лид (Утверждающий)
    @ManyToOne
    @JoinColumn(name = "lead_id")
    private User lead;

    // Ссылка на департамент (создадим сущность Department позже или используем String)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "created_at")
    // @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    // @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime completedAt;

    @Column(name = "updated_at")
    // @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;

    @Column(name = "spent_hours")
    private Integer spentHours;

    @Column(name = "deadline_at")
    // @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime deadlineAt;
}