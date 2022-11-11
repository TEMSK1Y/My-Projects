package com.softserve.itacademy.dto.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import com.softserve.itacademy.model.User;
import lombok.Value;

@Value
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;

    public UserResponse(User user) {
        id = user.getId();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        email = user.getEmail();
        password = user.getPassword();
        role = user.getRole().getName();
    }
}
