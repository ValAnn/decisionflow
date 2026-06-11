package com.decisiontool.decisionflow.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "developer_skills")
@Getter @Setter
public class DeveloperSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private DeveloperProfile profile;
    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;
    private boolean isPrimary;
}
