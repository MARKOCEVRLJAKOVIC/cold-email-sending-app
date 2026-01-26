package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpType;
import jakarta.mail.MessagingException;

/**
 * Interface for email sending implementations.
 * Each implementation supports a specific SMTP type.
 */
public interface EmailSender {
    /**
     * Returns the SMTP type that this sender supports.
     *
     * @return the supported SMTP type
     */
    SmtpType supports();

    /**
     * Sends an email message.
     *
     * @param emailMessage the email message to send
     * @throws MessagingException if sending fails
     */
    void sendEmail(EmailMessage emailMessage) throws MessagingException;
}
