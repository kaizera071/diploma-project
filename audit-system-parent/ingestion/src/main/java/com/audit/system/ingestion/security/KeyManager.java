package com.audit.system.ingestion.security;

import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyManager {
    @Value("${my.encryption.aes-key}")
    private String key;
    private SecretKey secretKey;

    @PostConstruct
    public void init() throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        secretKey = new SecretKeySpec(decodedKey, "AES");
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}