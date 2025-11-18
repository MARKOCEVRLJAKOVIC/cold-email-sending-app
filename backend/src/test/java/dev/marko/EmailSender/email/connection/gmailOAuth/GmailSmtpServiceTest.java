package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.email.connection.EmailConnectionService;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.security.TokenEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GmailSmtpServiceTest {

    @Mock private EmailConnectionService emailConnectionService;
    @Mock private OAuthTokenService oAuthTokenService;
    @Mock private SmtpRepository smtpRepository;
    @Mock private SmtpMapper smtpMapper;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private TokenEncryptor tokenEncryptor;

    User user;
    SmtpCredentials smtp;
    SmtpDto smtpDto;

    @BeforeEach
    void setup(){



    }


}
