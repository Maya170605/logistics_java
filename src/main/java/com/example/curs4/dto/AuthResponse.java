package com.example.curs4.dto;

import com.example.curs4.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long id;
    private String username;
    private String email;
    private Role role;
    private List<String> roles;
    private String name;
    private String unp;
    private String activityType;
    private boolean verified;
}