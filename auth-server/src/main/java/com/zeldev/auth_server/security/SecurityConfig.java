package com.zeldev.auth_server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.POST;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.cors(Customizer.withDefaults());
        http.authorizeHttpRequests(req -> req
                .requestMatchers("/login").permitAll()
                .requestMatchers(POST, "/logout").permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }
}
