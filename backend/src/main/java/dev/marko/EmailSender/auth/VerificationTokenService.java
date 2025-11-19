package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.ConfirmationResponse;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.UserRepository;
import dev.marko.EmailSender.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VerificationTokenService {

    @Value("${app.url}")
    private String appUrl;

    private final VerificationTokenRepository verificationTokenRepository;
    private final NotificationEmailService notificationEmailService;
    private final UserRepository userRepository;

    public void sendVerificationEmail(User user){

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expirationDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(verificationToken);


        String confirmationUrl = appUrl + "/auth/confirm?token=" + token;

        notificationEmailService.sendEmail(
                user.getEmail(),
                "Verification of email address",
                notificationEmailService.confirmationMessage() + confirmationUrl
        );
    }

    public ConfirmationResponse confirmEmail(String token){

        var verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(TokenNotFoundException::new);

        if(verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        return new ConfirmationResponse(true, "Email is successfully confirmed, you can now sign in.");

    }
}
