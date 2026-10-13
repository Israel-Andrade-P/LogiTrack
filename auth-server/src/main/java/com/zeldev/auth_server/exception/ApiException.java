package com.zeldev.auth_server.exception;

/**
 * ApiException
 */
public class ApiException extends RuntimeException{
  public ApiException(String message) {
    super(message);
  }
}

