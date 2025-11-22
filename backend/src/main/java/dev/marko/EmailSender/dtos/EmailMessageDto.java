package dev.marko.EmailSender.dtos;

import dev.marko.EmailSender.entities.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailMessageDto {

    private Long id;
    private String recipientEmail;
    private String recipientName;
    private LocalDateTime sentAt;
    private String sentMessage;
    private Status status;
    private LocalDateTime scheduledAt;
    private Long userId;

}
