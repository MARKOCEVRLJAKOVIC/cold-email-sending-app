package dev.marko.EmailSender.email.gmailOAuth;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.SmtpRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class GmailConnectionService {

    private OAuthTokenService tokenService;
    private SmtpRepository smtpRepository;
    private AuthService authService;

    public void connectGmail(OAuthTokens tokens, String senderEmail){

        User user = authService.getCurrentUser();

        Optional<SmtpCredentials> existing = smtpRepository.findByEmailAndUserId(senderEmail, user.getId());

        SmtpCredentials smtpCredentials = existing.orElseGet(SmtpCredentials::new);


        smtpCredentials.setEmail(senderEmail);
        smtpCredentials.setSmtpHost("smtp.gmail.com");
        smtpCredentials.setSmtpPort(587);
        smtpCredentials.setSmtpUsername(senderEmail);
        smtpCredentials.setSmtpPassword(null);

        smtpCredentials.setOauthAccessToken(tokens.getAccessToken());

        if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().isEmpty()) {
            smtpCredentials.setOauthRefreshToken(tokens.getRefreshToken());
        } else if (existing.isEmpty()) {
            throw new IllegalStateException("Refresh token is missing on new connection");
        }

        long expiresAt = tokens.getExpiresIn() > 0
                ? System.currentTimeMillis() + tokens.getExpiresIn() * 1000L
                : System.currentTimeMillis() + 3600 * 1000L;
        smtpCredentials.setTokenExpiresAt(expiresAt);

        smtpCredentials.setSmtpType(SmtpType.OAUTH2);
        smtpCredentials.setUser(user);

        smtpRepository.save(smtpCredentials);

    }

    public SmtpCredentials refreshTokenIfNeeded(SmtpCredentials creds) {
        long now = System.currentTimeMillis();

        if (creds.getTokenExpiresAt() != null && creds.getTokenExpiresAt() - now < 60_000) {

            if (creds.getOauthRefreshToken() == null || creds.getOauthRefreshToken().isEmpty()) {
                throw new IllegalStateException("No refresh token available.");
            }

            OAuthTokens refreshed = tokenService.refreshAccessToken(creds.getOauthRefreshToken());

            creds.setOauthAccessToken(refreshed.getAccessToken());

            long expiresAt = now + refreshed.getExpiresIn() * 1000L;
            creds.setTokenExpiresAt(expiresAt);

            if (refreshed.getRefreshToken() != null && !refreshed.getRefreshToken().isEmpty()) {
                creds.setOauthRefreshToken(refreshed.getRefreshToken());
            }

            smtpRepository.save(creds);
        }

        return creds;
    }
}
