package com.audit.system.retrieval.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        byte[] ivAndEncrypted = Base64.getDecoder().decode(encryptedData);
        ByteBuffer buffer = ByteBuffer.wrap(ivAndEncrypted);
        byte[] iv = new byte[IV_LENGTH];
        buffer.get(iv);
        byte[] encryptedBytes = new byte[buffer.remaining()];
        buffer.get(encryptedBytes);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}