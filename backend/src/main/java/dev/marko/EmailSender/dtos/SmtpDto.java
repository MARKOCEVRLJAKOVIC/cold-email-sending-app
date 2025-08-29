package dev.marko.EmailSender.dtos;

import dev.marko.EmailSender.entities.SmtpType;
import lombok.Data;

@Data
public class SmtpDto {

    private Long id;
    private String email;
    private SmtpType smtpType;
    private Long userId;
    private boolean enabled;

}
