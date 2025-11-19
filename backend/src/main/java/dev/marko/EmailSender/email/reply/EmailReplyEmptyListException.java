package dev.marko.EmailSender.email.reply;

public class EmailReplyEmptyListException extends RuntimeException{
    public EmailReplyEmptyListException() {
        super("There are no email replies yet");
    }

    public EmailReplyEmptyListException(String message) {
        super(message);
    }
}
