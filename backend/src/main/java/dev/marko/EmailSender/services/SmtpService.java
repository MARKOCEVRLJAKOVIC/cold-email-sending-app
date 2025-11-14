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
public class SmtpService extends BaseService<
        SmtpCredentials,
        SmtpDto,
        RegisterEmailRequest,
        SmtpRepository
        > {

    private final SmtpMapper mapper;

    public SmtpService(
            SmtpRepository repository,
            CurrentUserProvider currentUserProvider,
            SmtpMapper mapper
    ) {
        super(repository, currentUserProvider, EmailNotFoundException::new);
        this.mapper = mapper;
    }

    @Override
    protected SmtpDto toDto(SmtpCredentials entity) {
        return mapper.toDto(entity);
    }

    @Override
    protected SmtpCredentials toEntity(RegisterEmailRequest request) {
        return mapper.toEntity(request);
    }

    @Override
    protected void updateEntity(SmtpCredentials entity, RegisterEmailRequest request) {
        mapper.update(request, entity);
    }

    @Override
    protected void setUser(SmtpCredentials entity, User user) {
        entity.setUser(user);
    }
}
