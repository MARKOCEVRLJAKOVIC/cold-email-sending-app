package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.SmtpCredentials;
import lombok.NoArgsConstructor;

import java.util.Properties;

@NoArgsConstructor
public class SmtpUtil {
    // Creates SMTP properties (host, port, auth, TLS)
    public static Properties buildSmtpProperties(SmtpCredentials smtp) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtp.getSmtpHost());
        properties.put("mail.smtp.port", smtp.getSmtpPort().toString());
        return properties;
    }
}
