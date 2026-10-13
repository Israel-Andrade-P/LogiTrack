package com.zeldev.auth_server.security;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AuthServerConfig {
  private final JwtConfiguration jwtConfiguration;
  @Value("${ui.app.url}")
  private String redirectUri;

  @Bean
  public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcRegisteredClientRepository(jdbcTemplate);
  }

  @Bean
  @Order(1)
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      RegisteredClientRepository registeredClientRepository) {
    http
        .cors(Customizer.withDefaults())
        .oauth2AuthorizationServer(authServer -> authServer
            .registeredClientRepository(registeredClientRepository)
            .tokenGenerator(tokenGenerator())
            .oidc(Customizer.withDefaults()));

    return http.build();
  }

  @Bean
  public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
    var jwtGenerator = UserJwtGenerator.init(new NimbusJwtEncoder(jwtConfiguration.jwkSource()));
    jwtGenerator.setJwtCustomizer(customizer());
    OAuth2TokenGenerator<OAuth2RefreshToken> refreshTokenGenerator = new ClientOAuth2RefreshTokenGenerator();

    return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
  }

  @Bean 
  public OAuth2TokenCustomizer<JwtEncodingContext> customizer() {
    return context -> {
      if(OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
        context.getClaims().claims(claims -> claims.put("authorities", getAuthorities(context)));
      }
    };
  }

  @Bean
  public CorsConfigurationSource configurationSource() {
    var corsConfig = new CorsConfiguration();
    corsConfig.setAllowCredentials(true);
    corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
    corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin"));
    corsConfig.setExposedHeaders(Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION));
    corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    corsConfig.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
  }

  @Bean
  public ApplicationRunner runner(RegisteredClientRepository registeredClientRepository) {
    return args -> {
      if (registeredClientRepository.findByClientId("client") == null) {
        try {
          var registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
              .clientId("client")
              .clientSecret("secret")
              .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
              .authorizationGrantTypes(types -> {
                types.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                types.add(AuthorizationGrantType.REFRESH_TOKEN);
              })
              .scopes(scopes -> {
                scopes.add(OidcScopes.OPENID);
                scopes.add(OidcScopes.PROFILE);
                scopes.add(OidcScopes.EMAIL);
              })
              .redirectUri(redirectUri)
              .postLogoutRedirectUri("http://127.0.0.1:9000")
              .clientSettings(ClientSettings.builder().build())
              .tokenSettings(TokenSettings.builder().refreshTokenTimeToLive(Duration.ofDays(90))
                  .accessTokenTimeToLive(Duration.ofDays(1)).build())
              .build();
          registeredClientRepository.save(registeredClient);
        } catch (Exception e) {
          log.error(e.getMessage());
        }
      }
    };
  }

  private String getAuthorities(JwtEncodingContext context) {
    return context.getPrincipal().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
  }
}
