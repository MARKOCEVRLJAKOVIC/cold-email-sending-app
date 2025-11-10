package dev.marko.EmailSender.email.connection;

import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokens;

public interface TokenService {
    OAuthTokens refreshAccessToken(String refreshToken);
}
