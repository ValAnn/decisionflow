package com.decisiontool.decisionflow.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private String status;
    private String priority;
    @Column(name = "required_specialization")
    private String requiredSpecialization;
    @ManyToOne
    @JoinColumn(name = "analyst_id")
    private User analyst;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "spent_hours")
    private Integer spentHours;
    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;
    @ManyToMany
    @JoinTable(
    name = "task_skills",
    joinColumns = @JoinColumn(name = "task_id"),
    inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @JsonManagedReference
    private Set<Skill> skills = new HashSet<>();
}
