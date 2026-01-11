package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.exception.BadRequestException;

public class EmailReplyEmptyListException extends BadRequestException {
    public EmailReplyEmptyListException() {
        super("There are no email replies yet");
    }

    public EmailReplyEmptyListException(String message) {
        super(message);
    }
}
