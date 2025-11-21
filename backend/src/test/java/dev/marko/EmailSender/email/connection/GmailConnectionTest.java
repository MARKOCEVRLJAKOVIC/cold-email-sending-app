package dev.marko.EmailSender.email.connection;

import dev.marko.EmailSender.email.connection.gmailOAuth.GmailConnectionService;
import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokenService;
import dev.marko.EmailSender.email.connection.gmailOAuth.OAuthTokens;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.security.EncryptionService;
import dev.marko.EmailSender.services.SmtpCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GmailConnectionTest {

    @Mock private OAuthTokenService tokenService;
    @Mock private CurrentUserProvider currentUserProvider;
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
        when(currentUserProvider.getCurrentUser()).thenReturn(mockUser);
        when(encryptionService.encrypt(anyString())).thenAnswer(inv -> "enc_" + inv.getArgument(0));
        when(encryptionService.decrypt(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().replace("enc_", ""));

    }

    @Test
    public void connectGmail_shouldSaveNewCredentials_whenRefreshTokenPresent() {

        OAuthTokens tokens = new OAuthTokens("access", "refresh", 3600, null, null);

        when(smtpService.findByEmailAndUser("test@gmail.com")).thenReturn(Optional.empty());

        gmailConnectionService.connect(tokens, "test@gmail.com");

        ArgumentCaptor<SmtpCredentials> smtpCaptor = ArgumentCaptor.forClass(SmtpCredentials.class);
        verify(smtpService).save(smtpCaptor.capture());
        verify(smtpCaptor).getValue().setOauthAccessToken(encryptionService.encrypt(tokens.getAccessToken()));

        SmtpCredentials saved = smtpCaptor.getValue();
        assertEquals("smtp.gmail.com", saved.getSmtpHost());
        assertEquals(SmtpType.GMAIL, saved.getSmtpType());
        assertEquals("enc_access", saved.getOauthAccessToken());
        assertEquals("enc_refresh", saved.getOauthRefreshToken());
        assertEquals(mockUser, saved.getUser());
    }

}
