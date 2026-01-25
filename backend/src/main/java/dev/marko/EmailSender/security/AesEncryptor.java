package dev.marko.EmailSender.security;

import dev.marko.EmailSender.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class AesEncryptor implements Encryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String PREFIX = "ENC::";

    @Value("${spring.token.encrypt}")
    private String secretKey;

    private SecretKeySpec getKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        if (decodedKey.length != 16 &&
                decodedKey.length != 24 &&
                decodedKey.length != 32)
        {
            throw new IllegalStateException("Invalid key length: " + decodedKey.length);
        }

        return new SecretKeySpec(decodedKey, "AES");
    }

    @Override
    public String encrypt(String value) {
        try {
            SecretKeySpec keySpec = getKey();

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public String decrypt(String value) {
        if (!isEncrypted(value)) {
            throw new IllegalArgumentException("Value not encrypted");
        }
        try {
            String withoutPrefix = value.substring(PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(withoutPrefix);

            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), parameterSpec);

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new EncryptionException("Failed to decrypt");
        }
    }

    @Override
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

}