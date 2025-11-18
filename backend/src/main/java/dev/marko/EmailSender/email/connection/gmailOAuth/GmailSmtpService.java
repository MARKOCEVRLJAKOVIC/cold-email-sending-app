package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GmailSmtpService {

    private final SmtpRepository smtpRepository;
    private final SmtpMapper smtpMapper;
    private final CurrentUserProvider currentUserProvider;
    private final GmailAccountConnector gmailAccountConnector;

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


        var smtpCredentials = smtpRepository.findByEmail(request.getSenderEmail()).orElseThrow(EmailNotFoundException::new);
        gmailAccountConnector.connectGmail(request, smtpCredentials);

        var smtpDto = smtpMapper.toDto(smtpCredentials);
        smtpDto.setSmtpType(SmtpType.OAUTH2);

        return smtpDto;
    }

    public void disconnectGoogleAccount(Long id){
        var user = currentUserProvider.getCurrentUser();

        var smtpCredentials = smtpRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(EmailNotFoundException::new);

        gmailAccountConnector.disconnectGmail(smtpCredentials);

        smtpRepository.save(smtpCredentials);
    }


}
