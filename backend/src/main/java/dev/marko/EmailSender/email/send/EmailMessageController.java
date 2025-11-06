package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/email-messages")
public class EmailMessageController {

    private final EmailMessageService emailMessageService;

    @GetMapping
    public ResponseEntity<List<EmailMessageDto>> findAllMessagesFromUser(){

        var emailMessageListDto = emailMessageService.findAllMessagesFromUser();
        return ResponseEntity.ok(emailMessageListDto);

    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<EmailMessageDto>> findAllMessagesFromCampaign(@PathVariable Long campaignId){

        var emailMessageListDto = emailMessageService.findAllMessagesFromCampaign(campaignId);
        return ResponseEntity.ok(emailMessageListDto);

    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailMessageDto> getEmailMessage(@PathVariable Long id){

        var emailMessageDto = emailMessageService.getEmailMessage(id);
        return ResponseEntity.ok(emailMessageDto);

    }

    @PostMapping("/send-batch")
    public ResponseEntity<List<EmailMessageDto>> sendBatchEmails(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scheduledAt") LocalDateTime scheduledAt,
            @RequestParam("templateId") Long templateId,
            @RequestParam("smtpId") List<Long> smtpIds,
            @RequestParam(value = "campaignId", required = false) Long campaignId
    ) {

        var emailMessageListDto = emailMessageService.sendBatchEmails(file,scheduledAt,templateId,smtpIds,campaignId);
        return ResponseEntity.status(HttpStatus.CREATED).body(emailMessageListDto);

    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailMessageDto> updateEmailMessage(@PathVariable Long id, @RequestBody UpdateEmailMessageRequest request){

        var emailMessageDto = emailMessageService.updateEmailMessage(id, request);
        return ResponseEntity.ok(emailMessageDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EmailMessageDto> deleteEmailMessage(@PathVariable Long id){

        emailMessageService.deleteEmailMessage(id);
        return ResponseEntity.noContent().build();

    }

}