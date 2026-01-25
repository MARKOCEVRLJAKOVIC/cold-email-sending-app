package dev.marko.EmailSender.exception;

public class EncryptionException extends BadRequestException{
    public EncryptionException(String message) {
        super(message);
    }
}
