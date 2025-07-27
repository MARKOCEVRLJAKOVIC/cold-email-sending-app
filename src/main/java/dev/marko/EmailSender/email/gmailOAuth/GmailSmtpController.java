package dev.marko.EmailSender.email.gmailOAuth;

import dev.marko.EmailSender.dtos.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gmail-smtp")
@RequiredArgsConstructor
public class GmailSmtpController {

    private final GmailConnectionService gmailConnectionService;
    private final OAuthTokenService oAuthTokenService;

    @PostMapping("/connect")
    public ResponseEntity<GenericResponse> connectGmail(@RequestBody GmailConnectRequest request) {

        OAuthTokens tokens = oAuthTokenService.exchangeCodeForTokens(request.getCode());

        gmailConnectionService.connectGmail(tokens, request.getSenderEmail());
        return ResponseEntity.ok(new GenericResponse("Gmail account is successfully connected."));

    }
}