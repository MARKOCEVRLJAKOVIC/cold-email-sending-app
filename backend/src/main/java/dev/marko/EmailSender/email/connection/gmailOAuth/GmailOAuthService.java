package dev.marko.EmailSender.email.connection.gmailOAuth;

import com.google.api.services.gmail.Gmail;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.email.reply.GmailServiceFactory;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;

@AllArgsConstructor
@Service
public class GmailOAuthService {

    private final OAuthTokenService oAuthTokenService;
    private final GoogleOAuth2Properties properties;
    private final GmailConnectionService gmailConnectionService;
    private final SmtpRepository smtpRepository;
    private final SmtpMapper smtpMapper;
    private final EncryptionService encryptionService;
    private final GmailServiceFactory gmailServiceFactory;
    private final GmailTokenManager gmailTokenManager;

    public String generateAuthUrl(){
        return  "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + properties.getClientId() +
                "&redirect_uri=" + properties.getRedirectUri() +
                "&response_type=code" +
                "&scope=https://mail.google.com/" +
                "&access_type=offline" +
                "&prompt=consent";
    }

    @Transactional
    public SmtpDto oAuthCallback(String code) throws GeneralSecurityException, IOException {

        OAuthTokens tokens = oAuthTokenService.exchangeCodeForTokens(code);

        SmtpCredentials tempCredentials = new SmtpCredentials();
        tempCredentials.setOauthAccessToken(tokens.getAccessToken());
        tempCredentials.setOauthRefreshToken(tokens.getRefreshToken());

        Gmail gmailService = gmailServiceFactory.createService(
                tokens.getAccessToken(),
                tokens.getRefreshToken()
        );

        String senderEmail = oAuthTokenService.fetchSenderEmail(gmailService);

        gmailConnectionService.connect(tokens, senderEmail);

        var smtpCredentials = smtpRepository.findByEmail(senderEmail)
                .orElseThrow(EmailNotFoundException::new);

        gmailTokenManager.setRefreshToken(smtpCredentials, tokens);

        if (smtpCredentials.getOauthRefreshToken() != null &&
                encryptionService.isEncrypted(smtpCredentials.getOauthRefreshToken())) {
            smtpCredentials.setOauthRefreshToken(
                    encryptionService.encryptIfNeeded(smtpCredentials.getOauthRefreshToken())
            );
        }

        smtpCredentials.setEnabled(true);
        smtpRepository.save(smtpCredentials);

        return smtpMapper.toDto(smtpCredentials);

    }

}
