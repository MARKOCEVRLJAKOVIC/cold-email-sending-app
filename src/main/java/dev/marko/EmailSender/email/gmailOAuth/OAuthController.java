package dev.marko.EmailSender.email.gmailOAuth;

import dev.marko.EmailSender.dtos.GenericResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class OAuthController {

    private final OAuthTokenService oAuthTokenService;
    private final GoogleOAuth2Properties properties;
    private final GmailConnectionService gmailConnectionService;


    @GetMapping("/oauth-url")
    public ResponseEntity<String> generateAuthUrl() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + properties.getClientId() +
                "&redirect_uri=" + properties.getRedirectUri() +
                "&response_type=code" +
                "&scope=https://mail.google.com/" +
                "&access_type=offline" +
                "&prompt=consent";

        return ResponseEntity.ok(url);

    }
        @GetMapping("/callback")
        public ResponseEntity<GenericResponse> oauthCallback (@RequestParam String code,
                                                              @RequestParam String senderEmail){

            OAuthTokens tokens = oAuthTokenService.exchangeCodeForTokens(code);

            gmailConnectionService.connectGmail(tokens, senderEmail);

            return ResponseEntity.ok(new GenericResponse("OAuth token saved successfully"));

        }

}
