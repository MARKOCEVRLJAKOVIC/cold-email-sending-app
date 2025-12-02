package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

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

//    private static LocalDateTime calculateActualScheduleTime(LocalDateTime requestedScheduledAt, Campaign campaign) {
//        LocalDateTime baseTime = (requestedScheduledAt != null) ? requestedScheduledAt : LocalDateTime.now();
//
//        ZoneId campaignZone = ZoneId.of(campaign.getTimezone());
//
//        // convert base time into local campaign time
//        ZonedDateTime campaignZonedTime = baseTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(campaignZone);
//
//        LocalTime localTime = campaignZonedTime.toLocalTime();
//        LocalDateTime localDateTime = campaignZonedTime.toLocalDateTime();
//
//        LocalTime startTime = LocalTime.of(9, 0);
//        LocalTime endTime = LocalTime.of(17, 0);
//
//        LocalDateTime finalScheduledAt;
//
//        if (localTime.isBefore(startTime)) {
//            // if its before 9:00, schedule it for 9:00 for the same day
//            finalScheduledAt = localDateTime.withHour(9).withMinute(0).withSecond(0).withNano(0);
//        } else if (localTime.isAfter(endTime) || localTime.equals(endTime)) {
//            // if its after 17:00, schedule it for 9:00 for NEXT day
//            finalScheduledAt = localDateTime.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
//        } else {
//            // if its between (9:00 - 16:59:59...), schedule it normally
//            finalScheduledAt = localDateTime;
//        }
//
//        return finalScheduledAt.atZone(campaignZone).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
//    }

//    private static LocalDateTime calculateActualScheduleTime(LocalDateTime requestedScheduledAt, Campaign campaign) {
//
//        String timezoneId = campaign.getTimezone();
//        ZoneId campaignZone;
//
//        if (timezoneId == null || timezoneId.trim().isEmpty()) {
//            campaignZone = ZoneId.systemDefault();
//        } else {
//            try {
//                campaignZone = ZoneId.of(timezoneId);
//            } catch (java.time.zone.ZoneRulesException e) {
//                campaignZone = ZoneId.systemDefault();
//
//            }
//        }
//
//        LocalDateTime baseTime = (requestedScheduledAt != null) ? requestedScheduledAt : LocalDateTime.now();
//
//        ZonedDateTime campaignZonedTime = baseTime
//                .atZone(ZoneId.systemDefault())
//                .withZoneSameInstant(campaignZone);
//
//        LocalTime localTime = campaignZonedTime.toLocalTime();
//        LocalDateTime localDateTime = campaignZonedTime.toLocalDateTime();
//
//        LocalTime startTime = LocalTime.of(9, 0);
//        LocalTime endTime = LocalTime.of(17, 0);
//
//        LocalDateTime finalScheduledAt;
//
//        if (localTime.isBefore(startTime)) {
//            finalScheduledAt = localDateTime.withHour(9).withMinute(0).withSecond(0).withNano(0);
//        } else if (localTime.isAfter(endTime) || localTime.equals(endTime)) {
//            finalScheduledAt = localDateTime.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
//        } else {
//            finalScheduledAt = localDateTime;
//        }
//
//
//        return finalScheduledAt.atZone(campaignZone).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
//
//    }

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

//    public static EmailMessage createPendingMessage(
//            String recipientEmail,
//            String recipientName,
//            String messageText,
//            User user,
//            EmailTemplate template,
//            SmtpCredentials smtp,
//            Campaign campaign,
//            LocalDateTime requestedScheduledAt
//    ) {
//
//
//        LocalDateTime finalScheduledAt = calculateActualScheduleTime(requestedScheduledAt, campaign);
//
//        return buildMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign,
//                Status.PENDING, null, finalScheduledAt, null);
//    }

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

//    public static EmailMessage createMessageBasedOnSchedule(
//            String recipientEmail,
//            String recipientName,
//            String messageText,
//            User user,
//            EmailTemplate template,
//            SmtpCredentials smtp,
//            Campaign campaign,
//            LocalDateTime scheduledAt
//    ) {
//        // if scheduleAt is null calculateActualScheduleTime will be LocalDateTime.now()
//        LocalDateTime finalScheduledAt = calculateActualScheduleTime(scheduledAt, campaign);
//
//        // Check if the finalScheduleAt is in the future
//        // If finalScheduledAt == LocalDateTime.now() or in the past, send now
//        // Otherwise schedule (PENDING)
//        if (finalScheduledAt.isAfter(LocalDateTime.now().plusMinutes(1))) {
//            return createPendingMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign, finalScheduledAt);
//        } else {
//
//            return createSentMessage(recipientEmail, recipientName, messageText, user, template, smtp, campaign);
//        }
//    }

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
