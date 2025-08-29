package dev.marko.EmailSender.email.send;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEmailMessageRequest {

    private String sentMessage;
    private LocalDateTime scheduledAt;


}
