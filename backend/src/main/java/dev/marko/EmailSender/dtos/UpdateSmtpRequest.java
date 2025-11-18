package dev.marko.EmailSender.dtos;

import lombok.Data;

@Data
public class UpdateSmtpRequest {

    private String email;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;

}
