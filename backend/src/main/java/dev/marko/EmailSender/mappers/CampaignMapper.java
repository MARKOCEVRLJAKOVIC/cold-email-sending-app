package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CreateCampaignRequest;
import dev.marko.EmailSender.dtos.UpdateCampaignRequest;
import dev.marko.EmailSender.entities.Campaign;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "userId", source = "user.id")
    CampaignDto toDto(Campaign campaign);
    Campaign toEntity(CreateCampaignRequest request);

    @AfterMapping
    default void setDefaultTimezone(@MappingTarget Campaign campaign) {
        if (campaign.getTimezone() == null || campaign.getTimezone().isEmpty()) {
            campaign.setTimezone("Europe/Belgrade");
        }
    }


    List<CampaignDto> toListDto(List<Campaign> campaigns);

    void update(UpdateCampaignRequest request, @MappingTarget Campaign campaign);

}
