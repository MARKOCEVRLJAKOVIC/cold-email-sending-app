package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.GenericResponse;
import dev.marko.EmailSender.dtos.ResetPasswordConfirmRequest;
import dev.marko.EmailSender.dtos.ResetPasswordRequest;
import dev.marko.EmailSender.dtos.UserDto;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.UserNotFoundException;
import dev.marko.EmailSender.mappers.UserMapper;
import dev.marko.EmailSender.repositories.PasswordResetTokenRepository;
import dev.marko.EmailSender.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final NotificationEmailService notificationEmailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${app.url}")
    private String appUrl;


    public void forgotPassword(ResetPasswordRequest request){
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(UserNotFoundException::new);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        passwordResetTokenRepository.save(resetToken);

        String link = appUrl + "/password/forgot?token=" + token;

        notificationEmailService.sendEmail(request.getEmail(), "Reset your password using the following link: ", link);

    }

    public UserDto resetPassword(ResetPasswordConfirmRequest request){
        var token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Token"));

        if(token.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new TokenExpiredException();
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        var userDto = userMapper.toDto(user);

        passwordResetTokenRepository.delete(token);

        return userDto;
    }

}
