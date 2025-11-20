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

        String refreshToken = encryptionService.decryptIfNeeded(smtpCredentials.getOauthRefreshToken());

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalStateException("No refresh token available.");
        }

        try {
            OAuthTokens tokens = tokenService.refreshAccessToken(refreshToken);

            smtpCredentials.setOauthAccessToken(encryptionService.encryptIfNeeded(tokens.getAccessToken()));
            smtpCredentials.setTokenExpiresAt(now + tokens.getExpiresIn() * 1000L);

            setRefreshToken(smtpCredentials, tokens);

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

    public void setRefreshToken(SmtpCredentials smtpCredentials, OAuthTokens tokens) {
        if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().isEmpty()) {

            smtpCredentials.setOauthRefreshToken(encryptionService.encryptIfNeeded(tokens.getRefreshToken()));

        }
    }


}
