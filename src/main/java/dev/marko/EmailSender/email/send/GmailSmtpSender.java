package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.email.gmailOAuth.GmailConnectionService;
import dev.marko.EmailSender.email.gmailOAuth.OAuth2Authenticator;
import dev.marko.EmailSender.security.TokenEncryptor;
import jakarta.mail.*;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@AllArgsConstructor
public class GmailSmtpSender implements EmailSender {

    private final GmailConnectionService gmailConnectionService;
    private final TokenEncryptor tokenEncryptor;

    @Override
    public void sendEmails(EmailMessage email) throws MessagingException {

        SmtpCredentials smtp = email.getSmtpCredentials();

        if (!smtp.isGmailOauth()) {
            throw new IllegalArgumentException("Provided credentials are not Gmail OAuth2.");
        }

        smtp = gmailConnectionService.refreshTokenIfNeeded(smtp);
        smtp.setOauthAccessToken(tokenEncryptor.decryptIfNeeded(smtp.getOauthAccessToken()));
        smtp.setOauthRefreshToken(tokenEncryptor.decryptIfNeeded(smtp.getOauthRefreshToken()));

        Properties properties = getProperties(smtp);

        Authenticator auth = new OAuth2Authenticator(smtp.getEmail(), smtp.getOauthAccessToken());
        Session session = Session.getInstance(properties, auth);

        MimeMessage mimeMessage = getMimeMessage(email, session, smtp);

        if (email.getInReplyTo() != null) {
            mimeMessage.setHeader("In-Reply-To", email.getInReplyTo());
            mimeMessage.setHeader("References", email.getInReplyTo());
        }

        mimeMessage.saveChanges();
        String messageId = mimeMessage.getMessageID();
        email.setMessageId(messageId);

        Transport transport = session.getTransport("smtp");
        try {
            transport.connect();
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        }

        finally {
            transport.close();
        }

    }

    private static MimeMessage getMimeMessage(EmailMessage email, Session session, SmtpCredentials smtp) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(smtp.getEmail()));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(email.getRecipientEmail()));
        mimeMessage.setSubject(email.getEmailTemplate().getSubject());
        mimeMessage.setContent(email.getSentMessage(), "text/html; charset=utf-8");
        return mimeMessage;
    }

    private static Properties getProperties(SmtpCredentials smtp) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", smtp.getSmtpHost());
        properties.put("mail.smtp.port", String.valueOf(smtp.getSmtpPort()));
        properties.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", smtp.getSmtpHost());
        return properties;
    }
}