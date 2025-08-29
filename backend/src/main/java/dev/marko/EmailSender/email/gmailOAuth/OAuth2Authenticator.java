package dev.marko.EmailSender.email.gmailOAuth;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OAuth2Authenticator extends Authenticator {

    private final String email;
    private final String accessToken;

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(email, accessToken);
    }
}
