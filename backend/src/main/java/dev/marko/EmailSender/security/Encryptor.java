package dev.marko.EmailSender.security;

public interface Encryptor {
    String encrypt(String value);
    String decrypt(String value);
    boolean isEncrypted(String value);
}
