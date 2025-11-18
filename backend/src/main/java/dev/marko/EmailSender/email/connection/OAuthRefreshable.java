package dev.marko.EmailSender.email.connection;

import dev.marko.EmailSender.entities.SmtpCredentials;

public interface OAuthRefreshable {
    SmtpCredentials refreshTokenIfNeeded(SmtpCredentials smtpCredentials);
}
