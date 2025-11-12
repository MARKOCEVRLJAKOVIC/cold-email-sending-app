package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.dtos.GmailConnectionResponse;
import dev.marko.EmailSender.dtos.SmtpDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GmailOAuthController {

    private final GmailOAuthService gmailOAuthService;

    @GetMapping("/oauth-url")
    public ResponseEntity<GmailConnectionResponse> generateAuthUrl() {

        String url = gmailOAuthService.generateAuthUrl();
        return ResponseEntity.ok(new GmailConnectionResponse(url));

    }

    @GetMapping("/callback")
    public ResponseEntity<SmtpDto> oauthCallback(@RequestParam String code) throws Exception {


        var smtpDto = gmailOAuthService.oAuthCallback(code);
        return ResponseEntity.ok(smtpDto);
    }


}
