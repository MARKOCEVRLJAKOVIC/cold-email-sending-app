package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.security.EncryptionService;
import dev.marko.EmailSender.services.SmtpCredentialService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@AllArgsConstructor
@Service
public class GmailTokenManager {

    private final OAuthTokenService tokenService;
    private final SmtpCredentialService smtpService;
    private final EncryptionService encryptionService;

    public SmtpCredentials refreshIfNeeded(SmtpCredentials smtpCredentials) {

        long now = System.currentTimeMillis();

        // Token expires within 60 sec?
        if (smtpCredentials.getTokenExpiresAt() == null ||
                smtpCredentials.getTokenExpiresAt() - now > 60_000) {
            return smtpCredentials;
        }

        String refreshToken = encryptionService.decrypt(smtpCredentials.getOauthRefreshToken());

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalStateException("No refresh token available.");
        }

        try {
            OAuthTokens refreshed = tokenService.refreshAccessToken(refreshToken);

            smtpCredentials.setOauthAccessToken(encryptionService.encrypt(refreshed.getAccessToken()));
            smtpCredentials.setTokenExpiresAt(now + refreshed.getExpiresIn() * 1000L);

            if (refreshed.getRefreshToken() != null &&
                    !refreshed.getRefreshToken().isEmpty()) {

                smtpCredentials.setOauthRefreshToken(
                        encryptionService.encrypt(refreshed.getRefreshToken())
                );

                log.info("Updated refresh token for {}", smtpCredentials.getEmail());
            } else {
                log.info("Keeping existing refresh token for {}", smtpCredentials.getEmail());
            }

            return smtpService.save(smtpCredentials);

        } catch (HttpClientErrorException e) {

            String responseBody = e.getResponseBodyAsString();

            if (e.getStatusCode().is4xxClientError()
                    && responseBody.contains("invalid_grant")) {

                smtpCredentials.setOauthAccessToken(null);
                smtpCredentials.setOauthRefreshToken(null);
                smtpCredentials.setTokenExpiresAt(null);
                smtpCredentials.setEnabled(false);

                smtpService.save(smtpCredentials);

                throw new IllegalStateException(
                        "Refresh token is invalid or revoked for " + smtpCredentials.getEmail()
                );
            }

            throw e;
        }
    }
}
