package dev.marko.EmailSender.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateTemplateRequest {

    private String name;
    private String subject;
    @NotBlank
    private String message;
    @NotNull
    private Long campaignId;

}
