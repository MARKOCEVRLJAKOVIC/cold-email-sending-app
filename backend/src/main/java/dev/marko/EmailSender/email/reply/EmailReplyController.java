package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("reply")
public class EmailReplyController {

    private final EmailReplyService emailReplyService;

    @GetMapping
    public ResponseEntity<List<EmailReplyDto>> getAllRepliesFromUser(){

        var emailRepliesListDto = emailReplyService.getAllRepliesFromUser();
        return ResponseEntity.ok(emailRepliesListDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailReplyDto> getEmailReply(@PathVariable Long id){

        var emailReplyDto = emailReplyService.getEmailReply(id);
        return ResponseEntity.ok(emailReplyDto);

    }

    @PostMapping("/respond/{replyId}")
    public ResponseEntity<EmailMessageDto> replyToReply(@PathVariable Long replyId,
                                                        @RequestBody EmailReplyResponseDto response,
                                                        UriComponentsBuilder builder){

        var emailMessageDto = emailReplyService.respondToReply(replyId, response);
        var uri = builder.path("/reply/{id}").buildAndExpand(emailMessageDto.getId()).toUri();

        return ResponseEntity.created(uri).body(emailMessageDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long id){

        emailReplyService.deleteReply(id);
        return ResponseEntity.noContent().build();

    }
}
