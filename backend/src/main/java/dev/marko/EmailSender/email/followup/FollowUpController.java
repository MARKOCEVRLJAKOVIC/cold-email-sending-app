package dev.marko.EmailSender.email.followup;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@RestController
@RequestMapping("/follow-ups")
@AllArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    public ResponseEntity<List<FollowUpDto>> getAllFollowUps(){

        var followupDtoList = followUpService.getAllFollowUps();
        return ResponseEntity.ok(followupDtoList);

    }

    @GetMapping("/{id}")
    public ResponseEntity<FollowUpDto> getFollowUp(@PathVariable @Min(1) Long id){

        var followUpDto = followUpService.getFollowUp(id);
        return ResponseEntity.ok(followUpDto);

    }

    @PostMapping("/campaign/{campaignId}")
    public ResponseEntity<FollowUpDto> addFollowUpToCampaign(
            @PathVariable Long campaignId,
            @RequestBody @Valid CreateFollowUpRequest request,
            UriComponentsBuilder builder
    ) {

        var followUpDto = followUpService.addFollowUpToCampaign(campaignId, request);
        var uri = builder.path("/follow-ups/{id}").buildAndExpand(followUpDto.getId()).toUri();

        return ResponseEntity.created(uri).body(followUpDto);

    }

    @PutMapping("/{id}")
    public ResponseEntity<FollowUpDto> updateFollowUp(@PathVariable @Min(1) Long id,
                                                      @RequestBody @Valid CreateFollowUpRequest request){

        var followUpDto = followUpService.updateFollowUp(id, request);
        return ResponseEntity.ok(followUpDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFollowUp(@PathVariable @Min(1) Long id){

        followUpService.deleteFollowUp(id);
        return ResponseEntity.noContent().build();

    }

}