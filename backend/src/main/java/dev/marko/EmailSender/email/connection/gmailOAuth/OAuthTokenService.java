package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.email.connection.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Profile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OAuthTokenService implements TokenService {

    private final GoogleOAuth2Properties properties;
    private final RestTemplate restTemplate;

    public OAuthTokens exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("grant_type", "authorization_code");

        return postForTokens(params);
    }

    public OAuthTokens refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        return postForTokens(params);
    }

    private OAuthTokens postForTokens(MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<OAuthTokens> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token", request, OAuthTokens.class
        );

        return response.getBody();
    }

    public String fetchSenderEmail(Gmail service) throws IOException {
        Profile profile = service.users().getProfile("me").execute();
        return profile.getEmailAddress();
    }

}
