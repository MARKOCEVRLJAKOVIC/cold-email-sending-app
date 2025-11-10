package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.email.connection.EmailConnectionService;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.exception.MissingRefreshTokenException;
import dev.marko.EmailSender.security.EncryptionService;
import dev.marko.EmailSender.services.SmtpCredentialService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class GmailConnectionService implements EmailConnectionService {

    private final OAuthTokenService tokenService;
    private final SmtpCredentialService smtpService;
    private final AuthService authService;
    private final EncryptionService encryptionService;

    @Override
    public void connect(OAuthTokens tokens, String senderEmail) {

        var user = authService.getCurrentUser();

        Optional<SmtpCredentials> existing = smtpService.findByEmailAndUser(senderEmail);
        SmtpCredentials smtpCredentials = existing.orElseGet(SmtpCredentials::new);

        smtpCredentials.setEmail(senderEmail);
        smtpCredentials.setSmtpHost("smtp.gmail.com");
        smtpCredentials.setSmtpPort(587);
        smtpCredentials.setSmtpUsername(senderEmail);
        smtpCredentials.setSmtpPassword(null);
        smtpCredentials.setOauthAccessToken(encryptionService.encrypt(tokens.getAccessToken()));


        if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().isEmpty()) {
            smtpCredentials.setOauthRefreshToken(encryptionService.encrypt(tokens.getRefreshToken()));
        } else if (existing.isEmpty()) {
            throw new MissingRefreshTokenException("No refresh token available for Gmail account: " + smtpCredentials.getEmail());
        }

        long expiresAt = tokens.getExpiresIn() > 0
                ? System.currentTimeMillis() + tokens.getExpiresIn() * 1000L
                : System.currentTimeMillis() + 3600 * 1000L;
        smtpCredentials.setTokenExpiresAt(expiresAt);

        smtpCredentials.setSmtpType(SmtpType.OAUTH2);
        smtpCredentials.setUser(user);

        smtpService.save(smtpCredentials);

    }

    @Override
    public SmtpCredentials refreshTokenIfNeeded(SmtpCredentials creds) {
        long now = System.currentTimeMillis();

        if (creds.getTokenExpiresAt() != null && creds.getTokenExpiresAt() - now < 60_000) {

            var refreshToken = encryptionService.decrypt(creds.getOauthRefreshToken());

            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new IllegalStateException("No refresh token available.");
            }

            try {
                OAuthTokens refreshed = tokenService.refreshAccessToken(refreshToken);

                creds.setOauthAccessToken(encryptionService.encrypt(refreshed.getAccessToken()));

                creds.setTokenExpiresAt(now + refreshed.getExpiresIn() * 1000L);

                if (refreshed.getRefreshToken() != null && !refreshed.getRefreshToken().isEmpty()) {
                    creds.setOauthRefreshToken(encryptionService.encrypt(refreshed.getRefreshToken()));
                    log.info("Updated refresh token for {}", creds.getEmail());
                } else {
                    log.info("Keeping existing refresh token for {}", creds.getEmail());

                }

                smtpService.save(creds);

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                String responseBody = e.getResponseBodyAsString();
                if (e.getStatusCode().is4xxClientError() && responseBody.contains("invalid_grant")) {
                    creds.setOauthAccessToken(null);
                    creds.setOauthRefreshToken(null);
                    creds.setTokenExpiresAt(null);
                    creds.setEnabled(false);
                    smtpService.save(creds);

                    throw new IllegalStateException("Refresh token is invalid or revoked for " + creds.getEmail());
                } else {
                    throw e;
                }
            }
        }

        return creds;
    }


}
