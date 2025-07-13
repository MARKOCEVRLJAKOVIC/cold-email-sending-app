package dev.marko.EmailSender.dtos;

import lombok.Data;

@Data
public class SmtpDto {

    private Long id;
    private String email;
    private Long userId;

}
