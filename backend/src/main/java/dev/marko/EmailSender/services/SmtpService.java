package dev.marko.EmailSender.services;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SmtpService {

    private final SmtpRepository smtpRepository;
    private final SmtpMapper smtpMapper;
    private final CurrentUserProvider currentUserProvider;

    public List<SmtpDto> getAllEmails(){

        var user = currentUserProvider.getCurrentUser();
        var smtpList = smtpRepository.findAllByUserId(user.getId());

        return smtpMapper.smtpListToDtoList(smtpList);

    }

    public SmtpDto getEmail(Long id){

        var user = currentUserProvider.getCurrentUser();

        var smtp = smtpRepository.findByIdAndUserId(id, user.getId()).orElseThrow(EmailNotFoundException::new);
        return smtpMapper.toDto(smtp);

    }

    public SmtpDto registerEmail(RegisterEmailRequest request){

        var user = currentUserProvider.getCurrentUser();

        var smtp = smtpMapper.toEntity(request);
        smtpRepository.save(smtp);

        return smtpMapper.toDto(smtp);

    }

    public SmtpDto updateEmail(Long id, RegisterEmailRequest request){

        var user = currentUserProvider.getCurrentUser();

        var smtp = smtpRepository.findById(id)
                .orElseThrow(EmailNotFoundException::new);

        smtp.setUser(user);

        smtpMapper.update(request, smtp);

        smtpRepository.save(smtp);

        var smtpDto = smtpMapper.toDto(smtp);
        smtpDto.setId(smtp.getId());

        return smtpDto;

    }

    public void deleteEmail(Long id){
        var smtp = smtpRepository.findById(id).orElseThrow(EmailNotFoundException::new);

        smtpRepository.delete(smtp);
    }

}
