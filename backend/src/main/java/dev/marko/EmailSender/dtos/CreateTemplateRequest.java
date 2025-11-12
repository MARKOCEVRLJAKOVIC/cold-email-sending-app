package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateTemplateRequest {

    private String name;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    private Long campaignId;

}
