package dev.marko.EmailSender.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final Encryptor encryptor;

    public String encrypt(String value) {
        return encryptor.encrypt(value);
    }

    public String decrypt(String value) {
        return encryptor.decrypt(value);
    }

    public boolean isEncrypted(String value) {
        return encryptor.isEncrypted(value);
    }

    public String encryptIfNeeded(String value) {
        return isEncrypted(value) ? value : encrypt(value);
    }

    public String decryptIfNeeded(String value) {
        return isEncrypted(value) ? decrypt(value) : value;
    }

}