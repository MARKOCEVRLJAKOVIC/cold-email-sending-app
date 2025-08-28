package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.email.followup.FollowUpDto;
import dev.marko.EmailSender.email.followup.CreateFollowUpRequest;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FollowUpMapper {

    @Mapping(target = "campaignId", source = "campaign.id")
    @Mapping(source = "user.id", target = "userId")
    FollowUpDto toDto (FollowUpTemplate followUpTemplate);

    FollowUpTemplate toEntity(CreateFollowUpRequest request);

    List<FollowUpDto> toListDto(List<FollowUpTemplate> followUpList);

    void update(CreateFollowUpRequest request, @MappingTarget FollowUpTemplate followUp);

}
