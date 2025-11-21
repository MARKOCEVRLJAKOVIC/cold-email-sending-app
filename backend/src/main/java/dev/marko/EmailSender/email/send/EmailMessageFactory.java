package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.*;

import java.time.LocalDateTime;

public class EmailMessageFactory {

    private static EmailMessage baseMessage(
            String recipientEmail,
            String recipientName,
            String messageText,
            User user,
            EmailTemplate template,
            SmtpCredentials smtp,
            Campaign campaign
    ) {
        return EmailMessage.builder()
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .sentMessage(messageText)
                .user(user)
                .emailTemplate(template)
                .smtpCredentials(smtp)
                .campaign(campaign)
                .build();
    }

    private static EmailMessage buildMessage(
            String recipientEmail,
            String recipientName,
            String messageText,
            User user,
            EmailTemplate template,
            SmtpCredentials smtp,
            Campaign campaign,
            Status status,
            LocalDateTime sentAt,
            LocalDateTime scheduledAt,
            String errorMessage
    ) {
        EmailMessage msg = baseMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign);
        msg.setStatus(status);

        if (sentAt != null) msg.setSentAt(sentAt);
        if (scheduledAt != null) msg.setScheduledAt(scheduledAt);
        if (errorMessage != null) msg.setErrorMessage(errorMessage);

        return msg;
    }

    public static EmailMessage createSentMessage(
            String recipientEmail,
            String recipientName,
            String messageText,
            User user,
            EmailTemplate template,
            SmtpCredentials smtp,
            Campaign campaign
    ) {
        return buildMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign,
                Status.SENT, LocalDateTime.now(), null, null);
    }

    public static EmailMessage createFailedMessage(
            String recipientEmail,
            String recipientName,
            String messageText,
            User user,
            EmailTemplate template,
            SmtpCredentials smtp,
            Campaign campaign,
            String errorMessage
    ) {
        return buildMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign,
                Status.FAILED, null, null, errorMessage);
    }

    public static EmailMessage createPendingMessage(
            String recipientEmail,
            String recipientName,
            String messageText,
            User user,
            EmailTemplate template,
            SmtpCredentials smtp,
            Campaign campaign,
            LocalDateTime scheduledAt
    ) {
        return buildMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign,
                Status.PENDING, null, scheduledAt, null);
    }

    public static EmailMessage createMessageBasedOnSchedule(
            String recipientEmail,
            String recipientName,
            String messageText,
            User user,
            EmailTemplate template,
            SmtpCredentials smtp,
            Campaign campaign,
            LocalDateTime scheduledAt
    ) {
        return (scheduledAt != null)
                ? createPendingMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign, scheduledAt)
                : createSentMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign);
    }
}
