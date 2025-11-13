package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.services.base.BaseService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmtpService extends BaseService<SmtpCredentials, SmtpDto, RegisterEmailRequest, SmtpRepository> {

    private final SmtpMapper smtpMapper;

    protected SmtpService(SmtpRepository repository, CurrentUserProvider currentUserProvider, SmtpMapper smtpMapper) {
        super(repository, currentUserProvider);
        this.smtpMapper = smtpMapper;
    }

    @Override
    protected RuntimeException notFoundException() {
        return new EmailNotFoundException();
    }

    @Override
    protected SmtpDto toDto(SmtpCredentials entity) {
        return smtpMapper.toDto(entity);
    }

    @Override
    protected SmtpCredentials toEntity(RegisterEmailRequest createRequest) {
        return smtpMapper.toEntity(createRequest);
    }

    @Override
    protected void updateEntity(SmtpCredentials entity, RegisterEmailRequest request) {
        smtpMapper.update(request, entity);
    }

    @Override
    protected void setUserOnEntity(SmtpCredentials entity, User user) {
        entity.setUser(user);
    }

    @Override
    protected List<SmtpDto> toListDto(List<SmtpCredentials> listEntity) {
        return smtpMapper.smtpListToDtoList(listEntity);
    }


}
