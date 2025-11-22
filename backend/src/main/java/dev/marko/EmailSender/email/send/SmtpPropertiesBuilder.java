package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.SmtpCredentials;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class SmtpPropertiesBuilder {
    // Creates SMTP properties (host, port, auth, TLS)
    public Properties buildSmtpProperties(SmtpCredentials smtp) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtp.getSmtpHost());
        properties.put("mail.smtp.port", smtp.getSmtpPort().toString());
        return properties;
    }
}
