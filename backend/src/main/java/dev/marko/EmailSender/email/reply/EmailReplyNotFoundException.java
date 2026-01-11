package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.exception.ResourceNotFoundException;

public class EmailReplyNotFoundException extends ResourceNotFoundException {
    public EmailReplyNotFoundException() {
        super("Email Reply not found");
    }

    public EmailReplyNotFoundException(String message) {
        super(message);
    }

}
