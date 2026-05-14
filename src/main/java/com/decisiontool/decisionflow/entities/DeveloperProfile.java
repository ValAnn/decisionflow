package com.decisiontool.decisionflow.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "developer_profiles")
@Getter @Setter
@NoArgsConstructor
public class DeveloperProfile {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<DeveloperSkill> skills = new ArrayList<>();

    private String grade;

    public Optional<Skill> getSpecialization() {
    return this.skills.stream()
            .filter(DeveloperSkill::isPrimary)
            .map(DeveloperSkill::getSkill)
            .findFirst(); // Берем первый найденный основной навык
    }

    // @ManyToOne
    // @JoinColumn(name = "specialization_id")
    // private Skill specialization;
}