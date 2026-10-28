package com.zeldev.auth_server.service;

import com.zeldev.auth_server.entity.UserEntity;
import com.zeldev.auth_server.repository.UserRepository;
import com.zeldev.auth_server.request.LoginRequest;
import com.zeldev.auth_server.request.UserRequest;
import com.zeldev.auth_server.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.zeldev.auth_server.enumeration.RoleType.USER;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager manager;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;


    public void registerUser(UserRequest request) {
        userRepository.save(toUserEntity(request));
    }

    public LoginResponse login(LoginRequest request) {
        var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password());
        var authenticated = manager.authenticate(authRequest);
        var principal = (UserDetails) authenticated.getPrincipal();
        var roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        var token = jwtService.generateAccessToken(principal.getUsername(), roles);
        return new LoginResponse(token, "Bearer");
    }

    private UserEntity toUserEntity(UserRequest request) {
        return UserEntity.builder()
                .email(request.email())
                .password(encoder.encode(request.password()))
                .role(USER)
                .build();
    }
}
