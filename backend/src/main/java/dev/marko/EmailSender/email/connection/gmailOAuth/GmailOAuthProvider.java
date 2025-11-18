package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.entities.SmtpType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.smtp")
public class GmailOAuthProvider {

    private String host;
    private int port;
    private SmtpType type;
}
