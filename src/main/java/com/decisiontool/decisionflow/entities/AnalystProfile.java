package com.decisiontool.decisionflow.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analyst_profiles")
@Getter @Setter
@NoArgsConstructor
public class AnalystProfile {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String grade; // Junior, Middle, Senior, Lead
}
