package com.zeldev.auth_service.controller;

import com.zeldev.auth_service.request.LoginRequest;
import com.zeldev.auth_service.request.UserRequest;
import com.zeldev.auth_service.response.LoginResponse;
import com.zeldev.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRequest request) {
        userService.registerUser(request);
        return ResponseEntity.status(CREATED).body("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.status(OK).body(userService.login(request));
    }
}
