package dev.marko.EmailSender.security;

public interface EncryptionService {
    String encrypt(String value);
    String decrypt(String value);
    Boolean isEncrypted(String value);
}
