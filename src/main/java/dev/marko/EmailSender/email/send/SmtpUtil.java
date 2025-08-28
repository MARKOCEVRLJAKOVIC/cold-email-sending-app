package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.SmtpCredentials;

import java.util.Properties;

public class SmtpUtil {
    public static Properties buildSmtpProperties(SmtpCredentials smtp) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtp.getSmtpHost());
        properties.put("mail.smtp.port", smtp.getSmtpPort().toString());
        return properties;
    }
}
