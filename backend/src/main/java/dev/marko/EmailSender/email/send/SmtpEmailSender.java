package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@AllArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final SmtpPropertiesBuilder smtpPropertiesBuilder;

    @Override
    public SmtpType supports() {
        return SmtpType.PASSWORD;
    }

    @Override
    public void sendEmail(EmailMessage email) throws MessagingException {

        SmtpCredentials smtp = email.getSmtpCredentials();

        Properties properties = smtpPropertiesBuilder.buildSmtpProperties(smtp);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtp.getSmtpUsername(), smtp.getSmtpPassword());
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtp.getSmtpUsername()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipientEmail()));
        message.setSubject(email.getEmailTemplate().getSubject());
        message.setText(email.getSentMessage());

        Transport.send(message);

    }
}
