package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.email.connection.gmailOAuth.GmailConnectionService;
import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokenService;
import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokens;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.TokenEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GmailConnectionTest {

    @Mock private OAuthTokenService tokenService;
    @Mock private SmtpRepository smtpRepository;
    @Mock private AuthService authService;
    @Mock private TokenEncryptor tokenEncryptor;

    @InjectMocks
    private GmailConnectionService gmailConnectionService;

    private User mockUser;


    @BeforeEach
    public void setup() {

        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        when(authService.getCurrentUser()).thenReturn(mockUser);


    }

    @Test
    public void connectGmail_shouldSaveNewCredentials_whenRefreshTokenPresent() {

        OAuthTokens tokens = new OAuthTokens("access", "refresh", 3600, anyString(), anyString());
        String senderEmail = "user@gmail.com";

        var smtpCredentials = new SmtpCredentials();

        smtpCredentials.setTokenExpiresAt(anyLong());
        smtpCredentials.setSmtpType(SmtpType.OAUTH2);
        smtpCredentials.setUser(mockUser);



    }

}
