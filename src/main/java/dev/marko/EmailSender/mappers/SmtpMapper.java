package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.entities.SmtpCredentials;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SmtpMapper {

    @Mapping(target = "userId", source = "user.id")
    SmtpDto toDto(SmtpCredentials smtpCredentials);
    SmtpCredentials toEntity(RegisterEmailRequest request);

    void update(RegisterEmailRequest request, @MappingTarget SmtpCredentials smtpCredentials);
}
