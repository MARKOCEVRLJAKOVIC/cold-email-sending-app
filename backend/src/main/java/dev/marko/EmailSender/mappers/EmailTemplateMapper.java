package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.CreateTemplateRequest;
import dev.marko.EmailSender.dtos.EmailTemplateDto;
import dev.marko.EmailSender.dtos.UpdateTemplateRequest;
import dev.marko.EmailSender.entities.EmailTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "campaignId", source = "campaign.id")
    EmailTemplateDto toDto(EmailTemplate emailTemplate);

    EmailTemplate toEntity(CreateTemplateRequest request);

    List<EmailTemplateDto> toTemplateListDto(List<EmailTemplate> templates);

    void update(UpdateTemplateRequest request, @MappingTarget EmailTemplate emailTemplate);

}
