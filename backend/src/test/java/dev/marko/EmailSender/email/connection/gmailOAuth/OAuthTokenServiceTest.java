package dev.marko.EmailSender.email.connection.gmailOAuth;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthTokenServiceTest {

    @Mock
    private GoogleOAuth2Properties properties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OAuthTokenService oAuthTokenService;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> requestCaptor;

    @Test
    @DisplayName("Should exchange auth code for tokens with correct parameters")
    void shouldExchangeCodeForTokens() {
        // Given
        String code = "auth_code_123";
        setupPropertiesMock();

        OAuthTokens expectedTokens = new OAuthTokens();
        expectedTokens.setAccessToken("access_token_123");
        expectedTokens.setExpiresIn(3600);

        when(restTemplate.postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                any(HttpEntity.class),
                eq(OAuthTokens.class)
        )).thenReturn(new ResponseEntity<>(expectedTokens, HttpStatus.OK));

        // When
        OAuthTokens result = oAuthTokenService.exchangeCodeForTokens(code);

        // Then
        assertThat(result).isEqualTo(expectedTokens);

        verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), eq(OAuthTokens.class));

        MultiValueMap<String, String> body = requestCaptor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsEntry("code", java.util.Collections.singletonList(code));
        assertThat(body).containsEntry("client_id", java.util.Collections.singletonList("my-client-id"));
        assertThat(body).containsEntry("grant_type", java.util.Collections.singletonList("authorization_code"));
        assertThat(body).containsEntry("redirect_uri", java.util.Collections.singletonList("http://localhost:8080/cb"));
    }

    @Test
    @DisplayName("Should refresh access token with correct parameters")
    void shouldRefreshAccessToken() {
        // Given
        String refreshToken = "refresh_token_xyz";
        setupPropertiesMock();

        OAuthTokens expectedTokens = new OAuthTokens();
        expectedTokens.setAccessToken("new_access_token");

        when(restTemplate.postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                any(HttpEntity.class),
                eq(OAuthTokens.class)
        )).thenReturn(new ResponseEntity<>(expectedTokens, HttpStatus.OK));

        // When
        OAuthTokens result = oAuthTokenService.refreshAccessToken(refreshToken);

        // Then
        assertThat(result.getAccessToken()).isEqualTo("new_access_token");

        verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), eq(OAuthTokens.class));
        MultiValueMap<String, String> body = requestCaptor.getValue().getBody();

        assertThat(body).containsEntry("refresh_token", java.util.Collections.singletonList(refreshToken));
        assertThat(body).containsEntry("grant_type", java.util.Collections.singletonList("refresh_token"));
    }

    @Test
    @DisplayName("Should fetch sender email from Gmail API")
    void shouldFetchSenderEmail() throws IOException {
        // Given
        Gmail gmailMock = mock(Gmail.class);
        Gmail.Users usersMock = mock(Gmail.Users.class);
        Gmail.Users.GetProfile getProfileMock = mock(Gmail.Users.GetProfile.class);

        Profile profileResponse = new Profile();
        profileResponse.setEmailAddress("marko@test.dev");

        when(gmailMock.users()).thenReturn(usersMock);
        when(usersMock.getProfile("me")).thenReturn(getProfileMock);
        when(getProfileMock.execute()).thenReturn(profileResponse);

        // When
        String email = oAuthTokenService.fetchSenderEmail(gmailMock);

        // Then
        assertThat(email).isEqualTo("marko@test.dev");

        verify(getProfileMock).execute();
    }

    private void setupPropertiesMock() {
        when(properties.getClientId()).thenReturn("my-client-id");
        when(properties.getClientSecret()).thenReturn("my-secret");
        lenient().when(properties.getRedirectUri()).thenReturn("http://localhost:8080/cb");
    }
}