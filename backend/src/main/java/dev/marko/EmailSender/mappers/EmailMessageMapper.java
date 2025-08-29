package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.email.send.UpdateEmailMessageRequest;
import dev.marko.EmailSender.entities.EmailMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailMessageMapper {

    @Mapping(target = "userId", source = "user.id")
    EmailMessageDto toDto(EmailMessage emailMessage);

    List<EmailMessageDto> toListDto(List<EmailMessage> emailMessageList);

    void update(UpdateEmailMessageRequest request, @MappingTarget EmailMessage oldEmailMessage);
}
