package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ConfirmationResponse {

    private boolean success;
    private String message;

}
