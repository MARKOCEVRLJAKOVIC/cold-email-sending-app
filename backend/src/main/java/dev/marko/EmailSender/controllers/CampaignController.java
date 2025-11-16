package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.controllers.base.BaseController;
import dev.marko.EmailSender.dtos.*;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.services.CampaignService;
import dev.marko.EmailSender.services.base.BaseService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("campaigns")
public class CampaignController extends BaseController<CampaignDto, CreateCampaignRequest> {

    private final CampaignService campaignService;

    protected CampaignController(BaseService<?, CampaignDto, CreateCampaignRequest, ?> service, CampaignService campaignService) {
        super(service);
        this.campaignService = campaignService;
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<CampaignStatsDto> getCampaignStats(@PathVariable Long id){

        var stats = campaignService.getCampaignStats(id);
        return ResponseEntity.ok(stats);

    }

}
