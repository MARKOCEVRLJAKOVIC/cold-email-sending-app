package dev.marko.EmailSender.graphql;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.repositories.CampaignRepository;
import lombok.AllArgsConstructor;
import org.apache.catalina.LifecycleState;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class CampaignGraphQLController {

    private final CampaignRepository campaignRepository;

    @QueryMapping
    public List<Campaign> campaigns() {
        return campaignRepository.findAll();
    }

    public Campaign campaign(@Argument Long id) {
        return campaignRepository.findById(id).orElse(null);
    }



}
