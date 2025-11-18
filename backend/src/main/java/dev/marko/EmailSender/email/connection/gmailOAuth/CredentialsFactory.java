package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.security.EncryptionService;
import dev.marko.EmailSender.services.SmtpCredentialService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CredentialsFactory {

    private final EncryptionService encryptionService;
    private final GmailOAuthProvider provider;
    private final CurrentUserProvider userProvider;
    private final SmtpCredentialService smtpService;

    public SmtpCredentials createOrUpdate(
            String email,
            OAuthTokens tokens
    ) {

        Optional<SmtpCredentials> existing = smtpService.findByEmailAndUser(email);
        SmtpCredentials smtpCredentials = existing.orElseGet(SmtpCredentials::new);

        smtpCredentials.setEmail(email);
        smtpCredentials.setSmtpHost(provider.getHost());
        smtpCredentials.setSmtpPort(provider.getPort());
        smtpCredentials.setSmtpUsername(email);
        smtpCredentials.setSmtpPassword(null);
        smtpCredentials.setSmtpType(provider.getType());
        smtpCredentials.setUser(userProvider.getCurrentUser());

        smtpCredentials.setOauthAccessToken(
                encryptionService.encrypt(tokens.getAccessToken())
        );

        if (tokens.getRefreshToken() != null) {
            smtpCredentials.setOauthRefreshToken(
                    encryptionService.encrypt(tokens.getRefreshToken())
            );
        }

        smtpCredentials.setTokenExpiresAt(
                System.currentTimeMillis() + tokens.getExpiresIn() * 1000L
        );

        return smtpCredentials;
    }
}
