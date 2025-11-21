package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpType;
import jakarta.mail.MessagingException;

public interface EmailSender {
    SmtpType supports();
    void sendEmail(EmailMessage emailMessage) throws MessagingException;
}
