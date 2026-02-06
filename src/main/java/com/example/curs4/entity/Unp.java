package com.example.curs4.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "unp")
public class Unp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unp_id")
    private Long id;

    @Column(name = "unp", unique = true, nullable = false)
    private String unp;

}
