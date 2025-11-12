package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailTemplateDto {

    private Long id;
    private String name;
    private String subject;
    private String message;
    private Long campaignId;
    private Long userId;

}
