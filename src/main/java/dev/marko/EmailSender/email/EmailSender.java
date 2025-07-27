package dev.marko.EmailSender.email;

import dev.marko.EmailSender.entities.EmailMessage;
import jakarta.mail.MessagingException;

public interface EmailSender {
    void sendEmails(EmailMessage emailMessage) throws MessagingException;
}
