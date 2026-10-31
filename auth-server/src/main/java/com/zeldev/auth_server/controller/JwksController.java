package com.zeldev.auth_server.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/jwk")
@RequiredArgsConstructor
public class JwksController {
    private final JWKSource<SecurityContext> jwkSource;

    @GetMapping(value = "/keys",
                produces = APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() throws JOSEException {
        var keys = jwkSource.get(new JWKSelector(new JWKMatcher.Builder().build()), null);
        return new JWKSet(keys).toPublicJWKSet().toJSONObject();
    }
}
