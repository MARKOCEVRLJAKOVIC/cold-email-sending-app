package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailRecipientDto {

    String email;
    String name;

}
