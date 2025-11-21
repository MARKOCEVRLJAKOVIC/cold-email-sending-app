package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.email.connection.OAuthRefreshable;
import dev.marko.EmailSender.email.connection.gmailOAuth.OAuth2Authenticator;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.security.EncryptionService;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@AllArgsConstructor
public class GmailSmtpSender implements EmailSender {

    private final OAuthRefreshable refreshable;
    private final EncryptionService encryptionService;
    private final GmailSmtpMessageBuilder gmailSmtpMessageBuilder;

    @Override
    public SmtpType supports() {
        return SmtpType.GMAIL;
    }

    @Override
    public void sendEmail(EmailMessage email) throws MessagingException {

        SmtpCredentials smtp = email.getSmtpCredentials();

        if (!smtp.isGmailOauth()) {
            throw new IllegalArgumentException("Provided credentials are not Gmail OAuth2.");
        }

        // Refresh access token if expired and update SmtpCredentials
        smtp = refreshable.refreshTokenIfNeeded(smtp);
        String accessToken = encryptionService.decrypt(smtp.getOauthAccessToken());

        Properties properties = gmailSmtpMessageBuilder.getProperties(smtp);

        Authenticator auth = new OAuth2Authenticator(smtp.getEmail(), accessToken);
        Session session = Session.getInstance(properties, auth);

        // Build the MIME message (email, session, smtp)
        MimeMessage mimeMessage = gmailSmtpMessageBuilder.getMimeMessage(email, session, smtp);

        // If message is a reply, set threading headers
        if (email.getInReplyTo() != null) {
            mimeMessage.setHeader("In-Reply-To", email.getInReplyTo());
            mimeMessage.setHeader("References", email.getInReplyTo());
        }

        gmailSmtpMessageBuilder.markSent(mimeMessage, email);

        try (Transport transport = session.getTransport("smtp")) {
            transport.connect();
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        }

        catch (MessagingException e) {
            log.error("Failed to send email to {}", email.getRecipientEmail(), e);
            throw e;
        }

    }


}