package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.email.connection.gmailOAuth.GmailConnectionService;
import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokenService;
import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokens;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.EncryptionService;
import dev.marko.EmailSender.services.SmtpCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GmailConnectionTest {

    @Mock private OAuthTokenService tokenService;
    @Mock private SmtpRepository smtpRepository;
    @Mock private AuthService authService;
    @Mock private EncryptionService encryptionService;
    @Mock private SmtpCredentialService smtpService;


    @InjectMocks
    private GmailConnectionService gmailConnectionService;

    private User mockUser;

    @BeforeEach
    public void setup() {

        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(encryptionService.encrypt(anyString())).thenAnswer(inv -> "enc_" + inv.getArgument(0));
        when(encryptionService.decrypt(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().replace("enc_", ""));

    }

    @Test
    public void connectGmail_shouldSaveNewCredentials_whenRefreshTokenPresent() {

        OAuthTokens tokens = new OAuthTokens("access", "refresh", 3600, null, null);
        when(smtpService.findByEmailAndUser("test@gmail.com")).thenReturn(Optional.empty());

        gmailConnectionService.connect(tokens, "test@gmail.com");

        ArgumentCaptor<SmtpCredentials> captor = ArgumentCaptor.forClass(SmtpCredentials.class);
        verify(smtpService).save(captor.capture());

        SmtpCredentials saved = captor.getValue();
        assertEquals("smtp.gmail.com", saved.getSmtpHost());
        assertEquals(SmtpType.OAUTH2, saved.getSmtpType());
        assertEquals("enc_access", saved.getOauthAccessToken());
        assertEquals("enc_refresh", saved.getOauthRefreshToken());
        assertEquals(mockUser, saved.getUser());
    }

}
