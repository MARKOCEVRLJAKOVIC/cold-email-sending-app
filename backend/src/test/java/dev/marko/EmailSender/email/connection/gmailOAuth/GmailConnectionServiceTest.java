package dev.marko.EmailSender.email.connection.gmailOAuth;


import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.services.SmtpCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailConnectionServiceTest {

    @Mock
    private GmailTokenManager gmailTokenManager;

    @Mock
    private CredentialsFactory credentialsFactory;

    @Mock
    private SmtpCredentialService smtpCredentialService;

    @InjectMocks
    private GmailConnectionService gmailConnectionService;

    private OAuthTokens tokens;
    private SmtpCredentials smtpCredentials;

    @BeforeEach
    void setUp() {
        tokens = mock(OAuthTokens.class);
        smtpCredentials = mock(SmtpCredentials.class);
    }

    @Test
    void connect_shouldCreateOrUpdateCredentialsAndSaveThem() {
        // given
        String senderEmail = "test@gmail.com";
        when(credentialsFactory.createOrUpdate(senderEmail, tokens))
                .thenReturn(smtpCredentials);

        // when
        gmailConnectionService.connect(tokens, senderEmail);

        // then
        verify(credentialsFactory).createOrUpdate(senderEmail, tokens);
        verify(smtpCredentialService).save(smtpCredentials);
        verifyNoMoreInteractions(gmailTokenManager);
    }

    @Test
    void refreshTokenIfNeeded_shouldDelegateToGmailTokenManager() {
        // given
        SmtpCredentials refreshedCredentials = mock(SmtpCredentials.class);
        when(gmailTokenManager.refreshIfNeeded(smtpCredentials))
                .thenReturn(refreshedCredentials);

        // when
        SmtpCredentials result =
                gmailConnectionService.refreshTokenIfNeeded(smtpCredentials);

        // then
        verify(gmailTokenManager).refreshIfNeeded(smtpCredentials);
        assertSame(refreshedCredentials, result);
        verifyNoInteractions(credentialsFactory, smtpCredentialService);
    }
}
