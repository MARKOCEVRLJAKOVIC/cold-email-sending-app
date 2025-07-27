package dev.marko.EmailSender.email.gmailOAuth;

import jakarta.mail.MessagingException;

public class GmailOAuthException extends MessagingException {
    public GmailOAuthException(String message, Throwable cause) {
        super(message, (Exception) cause);
    }

    public GmailOAuthException() {
        super("Error during Gmail OAuth sending.");
    }
}
