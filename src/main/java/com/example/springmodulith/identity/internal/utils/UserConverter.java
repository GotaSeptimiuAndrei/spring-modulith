package com.example.springmodulith.identity.internal.utils;

import com.example.springmodulith.identity.User;
import com.example.springmodulith.identity.internal.dto.UserSignupRequest;

public class UserConverter {

    public static User convertToEntity(UserSignupRequest userDTO) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setIsAdmin(false);
        user.setUsername(userDTO.getUsername());
        return user;
    }

}
