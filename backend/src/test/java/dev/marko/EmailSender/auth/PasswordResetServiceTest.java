package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.ResetPasswordRequest;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.PasswordResetTokenRepository;
import dev.marko.EmailSender.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private NotificationEmailService notificationEmailService;

    @InjectMocks
    PasswordResetService passwordResetService;


    @Test
    public void resetPassword_ShouldReturnNotificationService(){



        ResetPasswordRequest request = new ResetPasswordRequest("marko@email.com");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername("Marko");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));


        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);

        passwordResetService.forgotPassword(request);

        verify(passwordResetTokenRepository, times(1)).save(tokenCaptor.capture());
        verify(notificationEmailService, times(1))
                .sendEmail(emailCaptor.capture(), subjectCaptor.capture(), linkCaptor.capture());

        assertEquals("marko@email.com", emailCaptor.getValue());
        assertEquals("Reset your password", subjectCaptor.getValue());

    }


}
