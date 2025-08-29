package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.dtos.*;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.services.CampaignService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailMessageMapper emailMessageMapper;

    @GetMapping
    public List<CampaignDto> getAllCampaignFromUser(){

        return campaignService.getAllCampaignFromUser();

    }


    @GetMapping("/{id}")
    public ResponseEntity<CampaignDto> getCampaign(@PathVariable Long id){

        var campaignDto = campaignService.getCampaign(id);
        return ResponseEntity.ok(campaignDto);

    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<CampaignStatsDto> getCampaignStats(@PathVariable Long id){

        var stats = campaignService.getCampaignStats(id);

        return ResponseEntity.ok(stats);

    }

    @GetMapping("{id}/replied")
    public List<EmailMessageDto> getRepliedEmails(@PathVariable Long id){

        var emailMessageList = emailMessageRepository.findAllByCampaignIdAndStatus(id, Status.REPLIED);

        return emailMessageList.stream().map(emailMessageMapper::toDto).toList();

    }

    @PostMapping
    public ResponseEntity<CampaignDto> createCampaign(@RequestBody CreateCampaignRequest request){

        var campaignDto = campaignService.createCampaign(request);
        return ResponseEntity.ok(campaignDto);

    }

    @PutMapping("{id}")
    public ResponseEntity<CampaignDto> updateCampaign(@PathVariable Long id,
                                                      @RequestBody CreateCampaignRequest request){

        var campaignDto = campaignService.updateCampaign(id,request);
        return ResponseEntity.ok(campaignDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id){

        campaignService.deleteCampaign(id);
        return ResponseEntity.accepted().build();

    }
}
