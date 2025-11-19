package dev.marko.EmailSender.exception;

public class EmailNotFoundException extends RuntimeException{
    public EmailNotFoundException() {
        super("Email account not found");
    }

    public EmailNotFoundException(String message) {
        super(message);
    }
}
