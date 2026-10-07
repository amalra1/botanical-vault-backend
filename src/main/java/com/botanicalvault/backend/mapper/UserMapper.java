package com.botanicalvault.backend.mapper;

import com.botanicalvault.backend.dto.response.UserResponseDTO;
import com.botanicalvault.backend.entity.User;

public class UserMapper {
    public static UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(user.getEmail());
    }
}
