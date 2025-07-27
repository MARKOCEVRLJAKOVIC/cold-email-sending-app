package dev.marko.EmailSender.email;

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

    public static EmailMessage createSentMessage(
            String recipientEmail,
            String recipientName,
            String messageText,
            User user,
            EmailTemplate template,
            SmtpCredentials smtp,
            Campaign campaign
    ) {
        EmailMessage msg = baseMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign);
        msg.setStatus(Status.SENT);
        msg.setSentAt(LocalDateTime.now());
        return msg;
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
        EmailMessage msg = baseMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign);
        msg.setStatus(Status.FAILED);
        return msg;
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
        EmailMessage msg = baseMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign);
        msg.setStatus(Status.PENDING);
        msg.setScheduledAt(scheduledAt);
        return msg;
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
        if (scheduledAt != null) {
            return createPendingMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign, scheduledAt);
        } else {
            return createSentMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign);
        }
    }
}
