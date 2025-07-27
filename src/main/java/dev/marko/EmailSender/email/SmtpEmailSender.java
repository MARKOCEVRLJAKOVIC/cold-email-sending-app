package dev.marko.EmailSender.email;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@AllArgsConstructor
public class SmtpEmailSender implements EmailSender {
    @Override
    public void sendEmails(EmailMessage email) throws MessagingException {

        SmtpCredentials smtp = email.getSmtpCredentials();

        Properties properties = SmtpUtil.buildSmtpProperties(smtp);


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
