package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.entities.EmailMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmailMessageMapper {

    @Mapping(target = "userId", source = "user.id")
    EmailMessageDto toDto(EmailMessage emailMessage);

}
