package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.GenericResponse;
import dev.marko.EmailSender.dtos.ResetPasswordConfirmRequest;
import dev.marko.EmailSender.dtos.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("forgot")
    public ResponseEntity<GenericResponse> forgotPassword(@RequestBody @Valid ResetPasswordRequest request){

        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(new GenericResponse("Password reset link sent to email"));

    }

    @PostMapping("reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordConfirmRequest request){

        var userDto = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(userDto);

    }

}
