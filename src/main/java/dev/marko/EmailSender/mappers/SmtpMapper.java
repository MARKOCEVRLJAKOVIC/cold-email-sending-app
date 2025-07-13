package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.controllers.SmtpController;
import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.entities.Smtp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SmtpMapper {

    @Mapping(target = "userId", source = "user.id")
    SmtpDto toDto(Smtp smtp);
    Smtp toEntity(RegisterEmailRequest request);

    void update(RegisterEmailRequest request, @MappingTarget Smtp smtp);
}
