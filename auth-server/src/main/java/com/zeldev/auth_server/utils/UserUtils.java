package com.zeldev.auth_server.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;

import com.zeldev.auth_server.model.User;

public class UserUtils {

  public static User getUser(Authentication authentication) {
    if(authentication instanceof OAuth2AuthorizationCodeRequestAuthenticationToken) {
      var usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication.getPrincipal();
      return (User) usernamePasswordAuthenticationToken.getPrincipal();
    }

    return (User) authentication.getPrincipal();
  }
}
