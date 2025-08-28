package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.email.reply.EmailReplyDto;
import dev.marko.EmailSender.entities.EmailReply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailReplyMapper {

    @Mapping(target = "emailMessageId", source = "emailMessage.id")
    EmailReplyDto toDto(EmailReply emailReply);
    EmailReply toEntity(EmailReplyDto emailReplyDto);

    List<EmailReplyDto> toListDto(List<EmailReply> emailRepliesList);



}
