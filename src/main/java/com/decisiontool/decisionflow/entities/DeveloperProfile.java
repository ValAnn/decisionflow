package com.decisiontool.decisionflow.entities;

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

    private String grade;

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private Skill specialization;
}