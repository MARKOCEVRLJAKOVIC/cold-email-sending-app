package dev.marko.EmailSender.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class AesEncryptor implements Encryptor {

    private static final String PREFIX = "ENC::";

    @Value("${spring.token.encrypt}")
    private String secretKey;

    private SecretKeySpec getKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(decodedKey, "AES");
    }

    @Override
    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            String encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
            return PREFIX + encrypted;
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public String decrypt(String value) {
        if (!isEncrypted(value)) {
            throw new IllegalArgumentException("Value is not encrypted");
        }
        try {
            String withoutPrefix = value.substring(PREFIX.length());
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            return new String(cipher.doFinal(Base64.getDecoder().decode(withoutPrefix)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }

    @Override
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

}