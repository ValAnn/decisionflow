package com.decisiontool.decisionflow.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    private String email;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "jira_id")
    private String jiraId;
    @Column(name = "jira_account_id")
    private String jiraAccountId;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
