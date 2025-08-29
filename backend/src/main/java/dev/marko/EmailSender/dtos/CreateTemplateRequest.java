package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class CreateTemplateRequest {

    private String name;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    private Long campaignId;

}
