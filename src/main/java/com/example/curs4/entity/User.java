package com.example.curs4.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name") // Убрали nullable = false
    private String name; // Теперь может быть null

    @ManyToOne
    @JoinColumn(name = "unp")
    private Unp unp;

    @Column
    private String email;

    @Column(name = "activity_type")
    private String activityType;

    private boolean verified;

    // Новые поля
    @Column(unique = true, nullable = false)
    private String username; // логин

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}