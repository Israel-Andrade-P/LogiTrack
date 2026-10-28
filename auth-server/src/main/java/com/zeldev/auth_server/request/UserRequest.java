package com.zeldev.auth_server.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Email field is required")
        @NotNull(message = "Email field is required")
        @Email(message = "Invalid email")
        String email,
        @NotBlank(message = "Password field is required")
        @NotNull(message = "Password field is required")
        @Size(min = 4, max = 30, message = "Password must be between 4 to 30 characters")
        String password
) {}
