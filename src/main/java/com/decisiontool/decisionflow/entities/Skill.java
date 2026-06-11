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
    private String category;
    @OneToMany(mappedBy = "skill")
    @JsonIgnore
    private List<DeveloperSkill> developerSkills;
    @ManyToMany(mappedBy = "skills")
    @JsonBackReference
    private Set<Task> tasks;
}
