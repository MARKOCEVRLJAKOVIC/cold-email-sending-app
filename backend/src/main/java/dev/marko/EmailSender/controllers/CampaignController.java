package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.controllers.base.BaseController;
import dev.marko.EmailSender.dtos.*;
import dev.marko.EmailSender.services.CampaignService;
import dev.marko.EmailSender.services.base.BaseService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("campaigns")
public class CampaignController extends BaseController<CampaignDto, CreateCampaignRequest, UpdateCampaignRequest> {

    private final CampaignService campaignService;

    protected CampaignController(BaseService<?, CampaignDto, CreateCampaignRequest, ?, UpdateCampaignRequest> service, CampaignService campaignService) {
        super(service);
        this.campaignService = campaignService;
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<CampaignStatsDto> getCampaignStats(@PathVariable @Min(1) Long id){

        var stats = campaignService.getCampaignStats(id);
        return ResponseEntity.ok(stats);

    }

}
