package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpType;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Delegates email sending to the appropriate EmailSender implementation based on SMTP type.
 */
@Service
public class EmailSenderDelegator {

    private final Map<SmtpType, EmailSender> senders;

    public EmailSenderDelegator(List<EmailSender> senders) {
        this.senders = senders.stream()
                .collect(Collectors.toMap(EmailSender::supports, s -> s));
    }

    /**
     * Sends an email message using the appropriate sender based on the message's SMTP type.
     *
     * @param message the email message to send
     * @throws MessagingException if sending fails
     */
    public void send(EmailMessage message) throws MessagingException {
        EmailSender sender = senders.get(message.getSmtpCredentials().getSmtpType());
        sender.sendEmail(message);
    }
}
