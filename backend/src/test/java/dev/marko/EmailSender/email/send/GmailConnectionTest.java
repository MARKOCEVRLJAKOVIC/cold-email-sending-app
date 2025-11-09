package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.email.gmailOAuth.GmailConnectionService;
import dev.marko.EmailSender.email.gmailOAuth.OAuthTokenService;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
    public void testConnection() {

        var smtpCredentials = new SmtpCredentials();

        smtpCredentials.setTokenExpiresAt(anyLong());
        smtpCredentials.setSmtpType(SmtpType.OAUTH2);
        smtpCredentials.setUser(mockUser);



    }

}
