package dev.marko.EmailSender.email.reply;

public class EmailReplyNotFoundException extends RuntimeException {
    public EmailReplyNotFoundException() {
        super("Email Reply not found");
    }

    public EmailReplyNotFoundException(String message) {
        super(message);
    }

}
