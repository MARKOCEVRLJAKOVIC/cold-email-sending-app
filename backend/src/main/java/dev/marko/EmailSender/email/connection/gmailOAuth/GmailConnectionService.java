package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.email.connection.EmailConnectionService;
import dev.marko.EmailSender.email.connection.OAuthRefreshable;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.services.SmtpCredentialService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class GmailConnectionService implements EmailConnectionService, OAuthRefreshable {

    private final GmailTokenManager gmailTokenManager;
    private final CredentialsFactory credentialsFactory;
    private final SmtpCredentialService smtpCredentialService;

    @Override
    public void connect(OAuthTokens tokens, String senderEmail) {

        var smtpCredentials = credentialsFactory.createOrUpdate(senderEmail, tokens);
        smtpCredentialService.save(smtpCredentials);

    }

    @Override
    public SmtpCredentials refreshTokenIfNeeded(SmtpCredentials smtpCredentials) {
        return gmailTokenManager.refreshIfNeeded(smtpCredentials);
    }


}
