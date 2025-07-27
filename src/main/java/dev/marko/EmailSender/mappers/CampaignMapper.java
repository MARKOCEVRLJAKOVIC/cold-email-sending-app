package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CreateCampaignRequest;
import dev.marko.EmailSender.entities.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "userId", source = "user.id")
    CampaignDto toDto(Campaign campaign);
    Campaign toEntity(CreateCampaignRequest request);

    void update(CreateCampaignRequest request, @MappingTarget Campaign campaign);

}
