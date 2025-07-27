package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class CampaignDto {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long userId;

}
