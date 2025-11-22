package dev.marko.EmailSender.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterEmailRequest {

    @Email(message = "Email should be a right format")
    @NotBlank(message = "Email must not be blank")
    @Size(min = 4, message = "Email must be over 4")
    private String email;

    @NotBlank(message = "Host must not be blank")
    @Size(min = 6, message = "Host must be over 6")
    private String smtpHost;

    @NotNull(message = "Port must not be null")
    private Integer smtpPort;

    @Size(min = 4, message = "Username must be over 4 characters")
    @NotBlank(message = "Username must not be blank")
    private String smtpUsername;

    @Size(min = 4, message = "Password must be over 4 and under 50 characters", max = 50)
    @NotBlank(message = "Password must not be blank")
    private String smtpPassword;

}
