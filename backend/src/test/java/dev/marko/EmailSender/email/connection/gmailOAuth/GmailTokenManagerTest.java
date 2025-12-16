package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.security.EncryptionService;
import dev.marko.EmailSender.services.SmtpCredentialService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailTokenManagerTest {

    @Mock
    private OAuthTokenService tokenService;

    @Mock
    private SmtpCredentialService smtpService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private GmailTokenManager gmailTokenManager;

    // --- Happy Path Scenarios ---

    @Test
    @DisplayName("Should NOT refresh token if it is still valid (expires in > 60s)")
    void shouldNotRefreshTokenIfValid() {
        // Given
        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setTokenExpiresAt(System.currentTimeMillis() + 300_000);

        // When
        SmtpCredentials result = gmailTokenManager.refreshIfNeeded(credentials);

        // Then
        assertThat(result).isSameAs(credentials);
        verifyNoInteractions(tokenService);
        verify(smtpService, never()).save(any());
    }

    @Test
    @DisplayName("Should refresh token successfully when expired")
    void shouldRefreshTokenWhenExpired() {
        // Given
        String encryptedRefreshToken = "enc_refresh_token";
        String decryptedRefreshToken = "raw_refresh_token";
        String newAccessToken = "new_access_token";
        String encryptedNewAccessToken = "enc_new_access_token";
        int expiresInSeconds = 3600;

        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setOauthRefreshToken(encryptedRefreshToken);
        credentials.setTokenExpiresAt(System.currentTimeMillis() - 1000);

        OAuthTokens newTokens = new OAuthTokens();
        newTokens.setAccessToken(newAccessToken);
        newTokens.setExpiresIn(expiresInSeconds);

        newTokens.setRefreshToken(null);

        // Mocking behavior
        when(encryptionService.decryptIfNeeded(encryptedRefreshToken)).thenReturn(decryptedRefreshToken);
        when(tokenService.refreshAccessToken(decryptedRefreshToken)).thenReturn(newTokens);
        when(encryptionService.encryptIfNeeded(newAccessToken)).thenReturn(encryptedNewAccessToken);
        when(smtpService.save(any(SmtpCredentials.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SmtpCredentials result = gmailTokenManager.refreshIfNeeded(credentials);

        // Then
        assertThat(result.getOauthAccessToken()).isEqualTo(encryptedNewAccessToken);
        assertThat(result.getTokenExpiresAt()).isGreaterThan(System.currentTimeMillis());

        verify(smtpService).save(credentials);
        verify(tokenService).refreshAccessToken(decryptedRefreshToken);
    }

    @Test
    @DisplayName("Should refresh token when it expires in less than 60 seconds")
    void shouldRefreshTokenWhenExpiringSoon() {

        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setOauthRefreshToken("enc_refresh");
        credentials.setTokenExpiresAt(System.currentTimeMillis() + 59_000);

        when(encryptionService.decryptIfNeeded(anyString())).thenReturn("raw_refresh");

        OAuthTokens tokens = new OAuthTokens();
        tokens.setAccessToken("new_access");
        tokens.setExpiresIn(3600);

        when(tokenService.refreshAccessToken("raw_refresh")).thenReturn(tokens);
        when(encryptionService.encryptIfNeeded("new_access")).thenReturn("enc_access");
        when(smtpService.save(any())).thenAnswer(i -> i.getArgument(0));

        gmailTokenManager.refreshIfNeeded(credentials);

        verify(tokenService).refreshAccessToken("raw_refresh");
    }


    @Test
    @DisplayName("Should update refresh token if API returns a new one")
    void shouldUpdateRefreshTokenIfNewOneReturned() {
        // Given
        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setOauthRefreshToken("old_enc_refresh");
        credentials.setTokenExpiresAt(System.currentTimeMillis() - 1000);

        OAuthTokens newTokens = new OAuthTokens();
        newTokens.setAccessToken("new_access");
        newTokens.setRefreshToken("new_refresh_token_raw");
        newTokens.setExpiresIn(3600);

        when(encryptionService.decryptIfNeeded(anyString())).thenReturn("old_raw_refresh");
        when(tokenService.refreshAccessToken(anyString())).thenReturn(newTokens);
        when(encryptionService.encryptIfNeeded("new_access")).thenReturn("enc_new_access");
        when(encryptionService.encryptIfNeeded("new_refresh_token_raw")).thenReturn("enc_new_refresh_token");
        when(smtpService.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        SmtpCredentials result = gmailTokenManager.refreshIfNeeded(credentials);

        // Then
        assertThat(result.getOauthRefreshToken()).isEqualTo("enc_new_refresh_token");
    }

    @Test
    @DisplayName("Should NOT refresh token when tokenExpiresAt is null")
    void shouldNotRefreshTokenWhenExpiresAtIsNull() {

        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setOauthRefreshToken("enc_token");
        credentials.setTokenExpiresAt(null);

        SmtpCredentials result = gmailTokenManager.refreshIfNeeded(credentials);

        assertThat(result).isSameAs(credentials);
        verifyNoInteractions(tokenService);
        verify(smtpService, never()).save(any());
    }


    // --- Error Handling Scenarios ---

    @Test
    @DisplayName("Should throw IllegalStateException if no refresh token is available")
    void shouldThrowExceptionIfRefreshTokenMissing() {
        // Given
        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setOauthRefreshToken(null);
        credentials.setTokenExpiresAt(System.currentTimeMillis() - 1000);

        when(encryptionService.decryptIfNeeded(null)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> gmailTokenManager.refreshIfNeeded(credentials))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No refresh token available");

        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Should handle 'invalid_grant' error by disabling credentials")
    void shouldHandleInvalidGrantError() {
        // Given
        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setEmail("test@gmail.com");
        credentials.setOauthRefreshToken("enc_token");
        credentials.setEnabled(true);
        credentials.setTokenExpiresAt(System.currentTimeMillis() - 1000);

        when(encryptionService.decryptIfNeeded(anyString())).thenReturn("raw_token");

        // Simulates 400 Bad Request with "invalid_grant" message
        HttpClientErrorException invalidGrantException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                "{\"error\": \"invalid_grant\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(tokenService.refreshAccessToken(anyString())).thenThrow(invalidGrantException);

        // When & Then
        assertThatThrownBy(() -> gmailTokenManager.refreshIfNeeded(credentials))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Refresh token is invalid or revoked");

        // Verify state changes
        ArgumentCaptor<SmtpCredentials> captor = ArgumentCaptor.forClass(SmtpCredentials.class);
        verify(smtpService).save(captor.capture());

        SmtpCredentials saved = captor.getValue();
        assertThat(saved.isEnabled()).isFalse();
        assertThat(saved.getOauthAccessToken()).isNull();
        assertThat(saved.getOauthRefreshToken()).isNull();
        assertThat(saved.getTokenExpiresAt()).isNull();
    }

    @Test
    @DisplayName("Should rethrow other HttpClientErrorExceptions (not invalid_grant)")
    void shouldRethrowOtherHttpErrors() {
        // Given
        SmtpCredentials credentials = new SmtpCredentials();
        credentials.setOauthRefreshToken("enc_token");
        credentials.setTokenExpiresAt(System.currentTimeMillis() - 1000);

        when(encryptionService.decryptIfNeeded(anyString())).thenReturn("raw_token");

        // Simulates 500 Internal Server Error (Google fall)
        HttpClientErrorException otherException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        when(tokenService.refreshAccessToken(anyString())).thenThrow(otherException);

        // When & Then
        assertThatThrownBy(() -> gmailTokenManager.refreshIfNeeded(credentials))
                .isInstanceOf(HttpClientErrorException.class)
                .isEqualTo(otherException);

        // Verify credentials were not disabled
        verify(smtpService, never()).save(any());
    }
}