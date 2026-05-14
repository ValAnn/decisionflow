package com.decisiontool.decisionflow.entities;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "skills")
@Getter @Setter
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String category; // Backend, Frontend, DevOps и т.д.

    // Связь с таблицей-переходником разработчиков
    @OneToMany(mappedBy = "skill")
    @JsonIgnore // Чтобы не было бесконечной рекурсии в JSON
    private List<DeveloperSkill> developerSkills;

    // Связь с задачами (если оставляешь ManyToMany для них)
    @ManyToMany(mappedBy = "skills") 
    @JsonBackReference
    private Set<Task> tasks;
}