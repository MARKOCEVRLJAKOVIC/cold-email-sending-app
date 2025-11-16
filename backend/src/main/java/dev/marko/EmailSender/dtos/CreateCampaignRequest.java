package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CreateCampaignRequest {

    private String name;
    private String description;

}
