package com.example.curs4.mapper;

import com.example.curs4.dto.UserDTO;
import com.example.curs4.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setActivityType(user.getActivityType());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());

        // Если есть поле unp в UserDTO
        if (user.getUnp() != null) {
            dto.setUnp(user.getUnp().getUnp());
        }

        return dto;
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setActivityType(dto.getActivityType());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setVerified(false);

        return user;
    }
}