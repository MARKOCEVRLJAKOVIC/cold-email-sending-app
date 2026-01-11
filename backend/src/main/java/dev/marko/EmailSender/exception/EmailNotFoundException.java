package dev.marko.EmailSender.exception;

public class EmailNotFoundException extends ResourceNotFoundException{
    public EmailNotFoundException() {
        super("Email account not found");
    }

    public EmailNotFoundException(String message) {
        super(message);
    }
}
