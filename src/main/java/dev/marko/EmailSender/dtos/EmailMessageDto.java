package dev.marko.EmailSender.dtos;

import dev.marko.EmailSender.entities.Status;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Data
public class EmailMessageDto {

    private Long id;
    private String recipientEmail;
    private String recipientName;
    private LocalDateTime sentAt;
    private String sentMessage;
    private Status status = Status.PENDING;
    private LocalDateTime scheduledAt;
    private Long userId;

}
