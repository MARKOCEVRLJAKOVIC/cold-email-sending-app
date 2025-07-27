package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CampaignStatsDto;
import dev.marko.EmailSender.dtos.CreateCampaignRequest;
import dev.marko.EmailSender.dtos.ErrorDto;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.exception.TemplateEmptyListException;
import dev.marko.EmailSender.mappers.CampaignMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.services.CampaignService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public List<CampaignDto> getAllCampaign(){

        return campaignService.getAllCampaign();

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

    @ExceptionHandler(CampaignNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCampaignException(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Campaign not found"));
    }

    @ExceptionHandler(TemplateEmptyListException.class)
    public ResponseEntity<ErrorDto> handleTemplateEmptyListException(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("There are no campaigns yet"));
    }

}
