package com.zeldev.auth_server.security;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.RSAKey;
import com.zeldev.auth_server.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KeyUtils {
  private static final String RSA = "RSA";
  @Value("${spring.profiles.active}")
  private String activeProfile;
  @Value("${keys.private}")
  private String privateKey;
  @Value("${keys.public}")
  private String publicKey;

  public RSAKey getRSAKeyPair() {
    return generateRSAKeyPair(privateKey, publicKey);
  }

  private RSAKey generateRSAKeyPair(String privateKeyName, String publicKeyName) {
    KeyPair keyPair;
    var keysDirectory = Paths.get("src", "main", "resources", "keys");
    verifyKeysDirectory(keysDirectory);

    if (Files.exists(keysDirectory.resolve(privateKeyName)) && Files.exists(keysDirectory.resolve(publicKeyName))) {
      log.info("RSA keys already exist, loading keys from file paths {}, {}", publicKeyName, privateKeyName);
      try {
        var privateKeyFile = keysDirectory.resolve(privateKeyName).toFile();
        var publicKeyFile = keysDirectory.resolve(publicKeyName).toFile();

        var keyFactory = KeyFactory.getInstance(RSA);

        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        var keyId = UUID.randomUUID().toString();
        log.info("Key ID: {}", keyId);

        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();
      } catch (Exception e) {
        log.error(e.getMessage());
        throw new ApiException(e.getMessage());
      }
    } else {
      if (activeProfile.equalsIgnoreCase("prod")) {
        throw new ApiException("Keys don't exist in prod");
      }

      log.info("Generating new public and private keys {}, {}", publicKeyName, privateKeyName);
      try {
        var keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        try (var fos = new FileOutputStream(keysDirectory.resolve(privateKeyName).toFile())) {
          PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
          fos.write(keySpec.getEncoded());
        }
        try (var fos = new FileOutputStream(keysDirectory.resolve(publicKeyName).toFile())) {
          X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
          fos.write(keySpec.getEncoded());
          return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
        }
      } catch (Exception e) {
        throw new ApiException(e.getMessage());
      }
    }
  }

  private void verifyKeysDirectory(Path keysDirectory) {
    if (!Files.exists(keysDirectory)) {
      try {
        Files.createDirectories(keysDirectory);
      } catch (Exception e) {
        log.error(e.getMessage());
        throw new ApiException(e.getMessage());
      }
      log.info("Created Keys directory: {}", keysDirectory);
    }
  }

}
