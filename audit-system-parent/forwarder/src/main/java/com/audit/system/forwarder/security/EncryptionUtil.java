package com.audit.system.forwarder.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = new byte[16];
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

}