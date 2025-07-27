package dev.marko.EmailSender.services;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SmtpService {

    private final SmtpRepository smtpRepository;
    private final SmtpMapper smtpMapper;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public List<SmtpDto> getAllEmail(){

        var user = authService.getCurrentUser();

        var smtpList = smtpRepository.findAllByUserId(user.getId());

        if(smtpList.isEmpty()){
            throw new EmailNotFoundException();
        }

        return smtpList.stream()
                .map(smtpMapper::toDto)
                .toList();

    }

    public SmtpDto getEmail(Long id){

        var smtp = smtpRepository.findById(id).orElseThrow(EmailNotFoundException::new);

        return smtpMapper.toDto(smtp);

    }

    public SmtpDto registerEmail(RegisterEmailRequest request){

        var user = authService.getCurrentUser();

        var smtp = smtpMapper.toEntity(request);
        smtp.setUser(user);
        smtpRepository.save(smtp);

        var smtpDto = smtpMapper.toDto(smtp);
        smtpDto.setId(smtp.getId());
        smtpDto.setUserId(user.getId());

        return smtpDto;

    }

    public SmtpDto updateEmail(Long id, RegisterEmailRequest request){

        var user = authService.getCurrentUser();

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
