package dev.marko.EmailSender.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegisterUserRequest {

    @Size(min = 4, message = "Username must be over 4 and under 15 characters", max = 15)
    private String username;
    @Size(min = 4, message = "Password must be over 4 and under 50 characters", max = 50)
    private String password;
    @Email
    @Size(min = 4, message = "Email must be over 4")
    private String email;



}
