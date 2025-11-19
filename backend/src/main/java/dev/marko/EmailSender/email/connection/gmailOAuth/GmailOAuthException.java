package dev.marko.EmailSender.email.connection.gmailOAuth;

import jakarta.mail.MessagingException;

public class GmailOAuthException extends MessagingException {
    public GmailOAuthException() {
        super("Gmail account not found");
    }

    public GmailOAuthException(String message) {
        super(message);
    }
}
