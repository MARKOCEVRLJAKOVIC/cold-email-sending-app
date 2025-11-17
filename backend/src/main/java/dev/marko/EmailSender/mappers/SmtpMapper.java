package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.dtos.UpdateSmtpRequest;
import dev.marko.EmailSender.entities.SmtpCredentials;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SmtpMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(source = "enabled", target = "enabled")
    SmtpDto toDto(SmtpCredentials smtpCredentials);
    SmtpCredentials toEntity(RegisterEmailRequest request);

    List<SmtpDto> smtpListToDtoList(List<SmtpCredentials> smtpList);


    void update(UpdateSmtpRequest request, @MappingTarget SmtpCredentials smtpCredentials);
}
