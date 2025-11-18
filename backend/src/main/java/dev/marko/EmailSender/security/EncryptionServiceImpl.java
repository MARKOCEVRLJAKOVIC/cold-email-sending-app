package dev.marko.EmailSender.security;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EncryptionServiceImpl implements EncryptionService {

    private final TokenEncryptor tokenEncryptor;

    @Override
    public String encrypt(String value) {
        return tokenEncryptor.encryptIfNeeded(value);
    }

    @Override
    public String decrypt(String value) {
        return tokenEncryptor.decryptIfNeeded(value);
    }

    @Override
    public Boolean isEncrypted(String value) {
        return tokenEncryptor.isEncrypted(value);
    }
}
