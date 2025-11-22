package dev.marko.EmailSender.email.connection;

import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokens;

public interface EmailConnectionService {

    void connect(OAuthTokens tokens, String senderEmail);

}
