package dev.marko.EmailSender.email;

import dev.marko.EmailSender.dtos.ErrorDto;
import dev.marko.EmailSender.dtos.GenericResponse;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;



@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/email-message")
public class EmailMessageController {

    private final EmailMessageService emailMessageService;

    @PostMapping("/send-batch")
    public ResponseEntity<GenericResponse> sendBatchEmails(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scheduledAt") LocalDateTime scheduledAt,
            @RequestParam("templateId") Long templateId,
            @RequestParam("smtpId") List<Long> smtpIds,
            @RequestParam(value = "campaignId", required = false) Long campaignId
    ) {

        emailMessageService.sendBatchEmails(file, scheduledAt, templateId, smtpIds, campaignId);


        return ResponseEntity.ok(new GenericResponse("Emails processed"));
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTemplateNotFoundException(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Template not found"));
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEmailNotFoundException(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Email not found"));
    }

}