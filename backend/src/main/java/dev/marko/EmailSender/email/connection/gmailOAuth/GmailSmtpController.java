package dev.marko.EmailSender.email.connection.gmailOAuth;

import dev.marko.EmailSender.dtos.SmtpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/gmail-smtp")
@RequiredArgsConstructor
public class GmailSmtpController {

    private final GmailSmtpService gmailSmtpService;

    @GetMapping
    public ResponseEntity<List<SmtpDto>> getAllEmailsFromUser(){

        var smtpDtoList = gmailSmtpService.getAllEmailsFromUser();
        return ResponseEntity.ok(smtpDtoList);

    }

    @GetMapping("/{id}")
    public ResponseEntity<SmtpDto> getEmail(@PathVariable Long id){

        var smtpDto = gmailSmtpService.getEmail(id);
        return ResponseEntity.ok(smtpDto);

    }

    @PostMapping
    public ResponseEntity<SmtpDto> connectGmail(@RequestBody GmailConnectRequest request,
                                                UriComponentsBuilder builder) {

        var smtpDto = gmailSmtpService.connectGmail(request);
        var uri = builder.path("/gmail-smtp/{id}").buildAndExpand(smtpDto.getId()).toUri();

        return ResponseEntity.created(uri).body(smtpDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disconnectGoogleAccount(@PathVariable Long id){

        gmailSmtpService.disconnectGoogleAccount(id);
        return ResponseEntity.noContent().build();

    }

}