package dev.marko.EmailSender.email.gmailOAuth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.oauth2")
public class GoogleOAuth2Properties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
}
