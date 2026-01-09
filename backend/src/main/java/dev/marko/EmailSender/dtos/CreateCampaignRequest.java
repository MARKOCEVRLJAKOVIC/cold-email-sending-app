package dev.marko.EmailSender.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCampaignRequest {

    @NotBlank
    private String name;
    private String description;
//    private String timezone;

}
