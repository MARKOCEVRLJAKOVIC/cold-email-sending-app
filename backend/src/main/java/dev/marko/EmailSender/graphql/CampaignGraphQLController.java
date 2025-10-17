package dev.marko.EmailSender.graphql;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CampaignStatsDto;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.AllArgsConstructor;
import org.apache.catalina.LifecycleState;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class CampaignGraphQLController {

    private final CampaignRepository campaignRepository;
    private final EmailMessageRepository emailMessageRepository;

    @QueryMapping
    public List<Campaign> campaigns() {
        return campaignRepository.findAll();
    }

    @QueryMapping
    public Campaign campaign(@Argument Long id) {
        return campaignRepository.findById(id).orElse(null);
    }

    @SchemaMapping
    public CampaignStatsDto stats(Campaign campaign) {

        List<EmailMessage> emails = emailMessageRepository.findAllByCampaign(campaign);

        int total = emails.size();
        int sent = (int) emails.stream()
                .filter(e -> e.getStatus() == Status.SENT || e.getStatus() == Status.REPLIED)
                .count();
        int failed = (int) emails.stream().filter(e -> e.getStatus() == Status.FAILED).count();
        int pending = (int) emails.stream().filter(e -> e.getStatus() == Status.PENDING).count();
        int replied = (int) emails.stream().filter(e -> e.getStatus() == Status.REPLIED).count();

        return new CampaignStatsDto(total, sent, failed, pending, replied);

    }

    @SchemaMapping(typeName = "Campaign", field = "emails")
    public List<EmailMessage> emails(Campaign campaign) {
        return emailMessageRepository.findAllByCampaign(campaign);
    }

}
