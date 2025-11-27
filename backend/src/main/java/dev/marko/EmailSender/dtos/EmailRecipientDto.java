package dev.marko.EmailSender.dtos;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailRecipientDto {

    String name;
    @Email(message = "Invalid email format")
    String email;

}
