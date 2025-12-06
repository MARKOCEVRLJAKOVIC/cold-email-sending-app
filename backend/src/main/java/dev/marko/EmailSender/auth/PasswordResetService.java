package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.ResetPasswordConfirmRequest;
import dev.marko.EmailSender.dtos.ResetPasswordRequest;
import dev.marko.EmailSender.dtos.UserDto;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.UserNotFoundException;
import dev.marko.EmailSender.mappers.UserMapper;
import dev.marko.EmailSender.repositories.PasswordResetTokenRepository;
import dev.marko.EmailSender.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final NotificationEmailService notificationEmailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${app.frontend-url}")
    private String appUrl;



    public void forgotPassword(ResetPasswordRequest request){
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(UserNotFoundException::new);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                .expiresAt(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(30))
                .build();

        passwordResetTokenRepository.save(resetToken);

        String link = appUrl + "/password/reset?token=" + token;

        String subject = "Reset your password";

        Map<String, String> variables = Map.of(
                "username", user.getUsername(),
                "resetLink", link
        );

        String body = notificationEmailService.buildEmailFromTemplate("password_reset_email.txt", variables);


        notificationEmailService.sendEmail(request.getEmail(), subject, body);

    }

    public UserDto resetPassword(ResetPasswordConfirmRequest request){
        var token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Token"));

        if(token.getExpiresAt().isBefore(LocalDateTime.now(ZoneId.of("UTC")))){
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
