package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.email.connection.EmailConnectionService;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GmailAccountConnector {

    @Value("${google.oauth2.redirect-uri}")
    private String revokeUrl;

    private final EmailConnectionService emailConnectionService;
    private final OAuthTokenService oAuthTokenService;
    private final EncryptionService encryptionService;
    private final RestTemplate restTemplate;

    public void connectGmail(GmailConnectRequest request, SmtpCredentials smtpCredentials){

        OAuthTokens tokens = oAuthTokenService.exchangeCodeForTokens(request.getCode());

        emailConnectionService.connect(tokens, request.getSenderEmail());


        if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().isEmpty()) {
            smtpCredentials.setOauthRefreshToken(encryptionService.encrypt(tokens.getRefreshToken()));
        } else if (smtpCredentials.getOauthRefreshToken() != null &&
                encryptionService.isEncrypted(smtpCredentials.getOauthRefreshToken())) {
            smtpCredentials.setOauthRefreshToken(encryptionService.encrypt(smtpCredentials.getOauthRefreshToken()));
        }
        smtpCredentials.setEnabled(true);

    }

    public void disconnectGmail(SmtpCredentials smtpCredentials) {


        if(smtpCredentials.getOauthRefreshToken() != null){
            try {
                String decrypted = encryptionService.decrypt(smtpCredentials.getOauthRefreshToken());
                revokeToken(decrypted);

            }
            catch (Exception e){
                System.err.println("Failed to revoke token for " + smtpCredentials.getEmail() + ": " + e.getMessage());

            }
        }

        smtpCredentials.setOauthAccessToken(null);
        smtpCredentials.setOauthRefreshToken(null);
        smtpCredentials.setTokenExpiresAt(null);

        smtpCredentials.setEnabled(false);

    }
    private void revokeToken(String token) {
        String url = revokeUrl + token;
        restTemplate.postForEntity(url, null, Void.class);
    }
}
