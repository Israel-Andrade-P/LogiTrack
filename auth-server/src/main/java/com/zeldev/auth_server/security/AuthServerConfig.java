package com.zeldev.auth_server.security;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AuthServerConfig {
  @Value("${ui.app.url}")
  private String redirectUri;

  @Bean
  public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcRegisteredClientRepository(jdbcTemplate);
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
}
