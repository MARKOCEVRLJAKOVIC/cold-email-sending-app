package dev.marko.EmailSender.email.gmailOAuth;

import com.google.api.services.gmail.Gmail;
import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.email.reply.GmailServiceFactory;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.TokenEncryptor;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class GmailOAuthController {

    private final OAuthTokenService oAuthTokenService;
    private final GoogleOAuth2Properties properties;
    private final GmailConnectionService gmailConnectionService;
    private final SmtpRepository smtpRepository;
    private  final AuthService authService;
    private final SmtpMapper smtpMapper;
    private final TokenEncryptor tokenEncryptor;
    private final GmailServiceFactory gmailServiceFactory;



    @GetMapping("/oauth-url")
    public ResponseEntity<Map<String, String>> generateAuthUrl() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + properties.getClientId() +
                "&redirect_uri=" + properties.getRedirectUri() +
                "&response_type=code" +
                "&scope=https://mail.google.com/" +
                "&access_type=offline" +
                "&prompt=consent";

        return ResponseEntity.ok(Map.of("url", url));

    }
    @GetMapping("/callback")
    public ResponseEntity<SmtpDto> oauthCallback(@RequestParam String code) throws Exception {

        var user = authService.getCurrentUser();

        OAuthTokens tokens = oAuthTokenService.exchangeCodeForTokens(code);

        SmtpCredentials tempCreds = new SmtpCredentials();
        tempCreds.setOauthAccessToken(tokens.getAccessToken());
        tempCreds.setOauthRefreshToken(tokens.getRefreshToken());

        Gmail gmailService = gmailServiceFactory.createService(
                tokens.getAccessToken(),
                tokens.getRefreshToken()
        );

        String senderEmail = oAuthTokenService.fetchSenderEmail(gmailService);

        gmailConnectionService.connectGmail(tokens, senderEmail);

        var smtpCredentials = smtpRepository.findByEmail(senderEmail)
                .orElseThrow(EmailNotFoundException::new);

        if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().isEmpty()) {
            smtpCredentials.setOauthRefreshToken(
                    tokenEncryptor.encryptIfNeeded(tokens.getRefreshToken())
            );
        } else if (smtpCredentials.getOauthRefreshToken() != null &&
                tokenEncryptor.isEncrypted(smtpCredentials.getOauthRefreshToken())) {
            smtpCredentials.setOauthRefreshToken(
                    tokenEncryptor.encryptIfNeeded(smtpCredentials.getOauthRefreshToken())
            );
        }

        smtpCredentials.setEnabled(true);
        smtpRepository.save(smtpCredentials);

        var smtpDto = smtpMapper.toDto(smtpCredentials);

        return ResponseEntity.ok(smtpDto);
    }


}
