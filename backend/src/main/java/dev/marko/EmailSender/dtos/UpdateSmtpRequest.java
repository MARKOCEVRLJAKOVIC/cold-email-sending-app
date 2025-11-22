package dev.marko.EmailSender.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSmtpRequest {

    @Email
    private String email;
    @NotBlank
    private String smtpHost;
    @NotNull
    private Integer smtpPort;
    @NotBlank
    private String smtpUsername;

}
