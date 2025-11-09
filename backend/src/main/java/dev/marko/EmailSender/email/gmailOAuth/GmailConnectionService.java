package dev.marko.EmailSender.email.gmailOAuth;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.TokenEncryptor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class GmailConnectionService {

    private final OAuthTokenService tokenService;
    private final SmtpRepository smtpRepository;
    private final AuthService authService;
    private final TokenEncryptor tokenEncryptor;

    public void connectGmail(OAuthTokens tokens, String senderEmail){

        var user = authService.getCurrentUser();

        Optional<SmtpCredentials> existing = smtpRepository.findByEmailAndUserId(senderEmail, user.getId());
        SmtpCredentials smtpCredentials = existing.orElseGet(SmtpCredentials::new);

        smtpCredentials.setEmail(senderEmail);
        smtpCredentials.setSmtpHost("smtp.gmail.com");
        smtpCredentials.setSmtpPort(587);
        smtpCredentials.setSmtpUsername(senderEmail);
        smtpCredentials.setSmtpPassword(null);
        smtpCredentials.setOauthAccessToken(tokenEncryptor.encryptIfNeeded(tokens.getAccessToken()));


        if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().isEmpty()) {
            smtpCredentials.setOauthRefreshToken(tokenEncryptor.encryptIfNeeded(tokens.getRefreshToken()));
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

            var refreshToken = tokenEncryptor.decryptIfNeeded(creds.getOauthRefreshToken());

            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new IllegalStateException("No refresh token available.");
            }

            try {
                OAuthTokens refreshed = tokenService.refreshAccessToken(refreshToken);

                creds.setOauthAccessToken(tokenEncryptor.encryptIfNeeded(refreshed.getAccessToken()));

                creds.setTokenExpiresAt(now + refreshed.getExpiresIn() * 1000L);

                if (refreshed.getRefreshToken() != null && !refreshed.getRefreshToken().isEmpty()) {
                    creds.setOauthRefreshToken(tokenEncryptor.encryptIfNeeded(refreshed.getRefreshToken()));
                    System.out.println("Updated refresh token for " + creds.getEmail());
                } else {
                    System.out.println("Keeping existing refresh token for " + creds.getEmail());
                }

                smtpRepository.save(creds);

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                String responseBody = e.getResponseBodyAsString();
                if (e.getStatusCode().is4xxClientError() && responseBody.contains("invalid_grant")) {
                    creds.setOauthAccessToken(null);
                    creds.setOauthRefreshToken(null);
                    creds.setTokenExpiresAt(null);
                    creds.setEnabled(false);
                    smtpRepository.save(creds);

                    throw new IllegalStateException("Refresh token is invalid or revoked for " + creds.getEmail());
                } else {
                    throw e;
                }
            }
        }

        return creds;
    }
}
