package dev.marko.EmailSender.dtos;

import lombok.Data;

@Data
public class ResetPasswordConfirmRequest {
    String token;
    String newPassword;
}
