package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.email.connection.EmailConnectionService;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.security.EncryptionService;
import dev.marko.EmailSender.security.TokenEncryptor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GmailSmtpService {

    @Value("${google.oauth2.revoke-uri}")
    private String revokeUrl;

    private final EmailConnectionService emailConnectionService;
    private final OAuthTokenService oAuthTokenService;
    private final SmtpRepository smtpRepository;
    private final SmtpMapper smtpMapper;
    private final CurrentUserProvider currentUserProvider;
    private final EncryptionService encryptionService;

    public List<SmtpDto> getAllEmailsFromUser(){

        var user = currentUserProvider.getCurrentUser();
        var smtpList = smtpRepository.findAllBySmtpTypeAndUserId(SmtpType.OAUTH2, user.getId());

        return smtpMapper.smtpListToDtoList(smtpList);

    }

    public SmtpDto getEmail(Long id){

        var user = currentUserProvider.getCurrentUser();

        var smtpCredentials = smtpRepository.findByIdAndUserId(id, user.getId()).orElseThrow(EmailNotFoundException::new);
        return smtpMapper.toDto(smtpCredentials);

    }

    public SmtpDto connectGmail(GmailConnectRequest request){

        OAuthTokens tokens = oAuthTokenService.exchangeCodeForTokens(request.getCode());

        emailConnectionService.connect(tokens, request.getSenderEmail());

        var smtpCredentials = smtpRepository.findByEmail(request.getSenderEmail()).orElseThrow(EmailNotFoundException::new);

        if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().isEmpty()) {
            smtpCredentials.setOauthRefreshToken(encryptionService.encrypt(tokens.getRefreshToken()));
        } else if (smtpCredentials.getOauthRefreshToken() != null &&
                encryptionService.isEncrypted(smtpCredentials.getOauthRefreshToken())) {
            smtpCredentials.setOauthRefreshToken(encryptionService.encrypt(smtpCredentials.getOauthRefreshToken()));
        }

        smtpCredentials.setEnabled(true);
        smtpRepository.save(smtpCredentials);

        var smtpDto = smtpMapper.toDto(smtpCredentials);
        smtpDto.setSmtpType(SmtpType.OAUTH2);

        return smtpDto;
    }

    public void disconnectGoogleAccount(Long id){
        var user = currentUserProvider.getCurrentUser();

        var smtpCredentials = smtpRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(EmailNotFoundException::new);

        if(smtpCredentials.getOauthRefreshToken() != null){
            try {
                String decrypted = encryptionService.decrypt(smtpCredentials.getOauthRefreshToken());
                revokeToken(decrypted);

            }
            catch (Exception e){
                log.error("Failed to revoke token for {}: {}", smtpCredentials.getEmail(), e.getMessage());
            }
        }

        smtpCredentials.setOauthAccessToken(null);
        smtpCredentials.setOauthRefreshToken(null);
        smtpCredentials.setTokenExpiresAt(null);

        smtpCredentials.setEnabled(false);
        smtpRepository.save(smtpCredentials);
    }


    private void revokeToken(String token) {
        RestTemplate restTemplate = new RestTemplate();
        String url = revokeUrl + token;
        restTemplate.postForEntity(url, null, Void.class);
    }
}
