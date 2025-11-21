package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class GmailSmtpMessageBuilder {

    public MimeMessage getMimeMessage(EmailMessage email, Session session, SmtpCredentials smtp) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(smtp.getEmail()));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(email.getRecipientEmail()));
        mimeMessage.setSubject(email.getEmailTemplate().getSubject());
        mimeMessage.setContent(email.getSentMessage(), "text/html; charset=utf-8");
        return mimeMessage;
    }

    public Properties getProperties(SmtpCredentials smtp) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", smtp.getSmtpHost());
        properties.put("mail.smtp.port", String.valueOf(smtp.getSmtpPort()));
        properties.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", smtp.getSmtpHost());
        return properties;
    }

    public void markSent(MimeMessage mimeMessage, EmailMessage email) throws MessagingException {
        mimeMessage.saveChanges();
        String messageId = mimeMessage.getMessageID();
        email.setMessageId(messageId);
    }


}
