package dev.marko.EmailSender.email.connection;

import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokens;
import dev.marko.EmailSender.entities.SmtpCredentials;

public interface EmailConnectionService {

    void connect(OAuthTokens tokens, String senderEmail);
//    SmtpCredentials refreshTokenIfNeeded(SmtpCredentials smtpCredentials);

}
