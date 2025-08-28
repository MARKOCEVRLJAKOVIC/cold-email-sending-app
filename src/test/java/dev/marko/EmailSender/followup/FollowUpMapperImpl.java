package dev.marko.EmailSender.followup;

import dev.marko.EmailSender.email.followup.FollowUpDto;
import dev.marko.EmailSender.mappers.FollowUpMapper;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-07T16:52:11+0200",
    comments = "version: 1.6.1, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class FollowUpMapperImpl implements FollowUpMapper {

    @Override
    public FollowUpDto toDto(FollowUpTemplate followUpTemplate) {
        if ( followUpTemplate == null ) {
            return null;
        }

        Long campaignId = null;
        Long id = null;
        String message = null;

        campaignId = followUpTemplateCampaignId( followUpTemplate );
        id = followUpTemplate.getId();
        message = followUpTemplate.getMessage();

        Long userId = null;

        FollowUpDto followUpDto = new FollowUpDto( id, message, userId, campaignId );

        return followUpDto;
    }

    private Long followUpTemplateCampaignId(FollowUpTemplate followUpTemplate) {
        Campaign campaign = followUpTemplate.getCampaign();
        if ( campaign == null ) {
            return null;
        }
        return campaign.getId();
    }
}
