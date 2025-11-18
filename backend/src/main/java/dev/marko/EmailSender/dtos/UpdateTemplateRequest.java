package dev.marko.EmailSender.dtos;

import lombok.Data;

@Data
public class UpdateTemplateRequest {

    private String name;
    private String subject;
    private String message;

}
