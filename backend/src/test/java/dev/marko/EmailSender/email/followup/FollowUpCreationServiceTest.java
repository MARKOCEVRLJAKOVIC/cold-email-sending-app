package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUpCreationService Tests")
public class FollowUpCreationServiceTest {

    @Mock
    private EmailMessageRepository emailMessageRepository;

    @Mock
    private EmailPreparationService emailPreparationService;

    @InjectMocks
    private FollowUpCreationService creationService;

    @Test
    @DisplayName("Should create follow-up with correct scheduled time")
    void shouldCreateFollowUpWithCorrectScheduledTime() {
        // Given
        LocalDateTime originalSentAt = LocalDateTime.of(2025, 1, 1, 10, 0);

        EmailMessage original = createOriginalEmail();
        original.setSentAt(originalSentAt);

        FollowUpTemplate template = new FollowUpTemplate();
        template.setDelayDays(3);
        template.setMessage("Hi {{name}}, following up!");

        when(emailPreparationService.generateMessageText(
                "Hi {{name}}, following up!", "John"
        )).thenReturn("Hi John, following up!");

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        when(emailMessageRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        EmailMessage result = creationService.createFollowUp(original, template);

        // Then
        EmailMessage saved = captor.getValue();
        assertThat(saved.getScheduledAt()).isEqualTo(originalSentAt.plusDays(3));
        assertThat(saved.getStatus()).isEqualTo(Status.PENDING);
        assertThat(saved.getInReplyTo()).isEqualTo(original.getMessageId());
        assertThat(saved.getSentMessage()).isEqualTo("Hi John, following up!");
        assertThat(saved.getFollowUpTemplate()).isEqualTo(template);
    }

    @Test
    @DisplayName("Should copy all necessary fields from original email")
    void shouldCopyFieldsFromOriginal() {
        // Given
        EmailMessage original = createOriginalEmail();
        FollowUpTemplate template = new FollowUpTemplate();
        template.setDelayDays(2);
        template.setMessage("Follow-up");

        when(emailPreparationService.generateMessageText(any(), any()))
                .thenReturn("Processed message");

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        when(emailMessageRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        creationService.createFollowUp(original, template);

        // Then
        EmailMessage saved = captor.getValue();
        assertThat(saved.getRecipientEmail()).isEqualTo(original.getRecipientEmail());
        assertThat(saved.getRecipientName()).isEqualTo(original.getRecipientName());
        assertThat(saved.getUser()).isEqualTo(original.getUser());
        assertThat(saved.getCampaign()).isEqualTo(original.getCampaign());
        assertThat(saved.getSmtpCredentials()).isEqualTo(original.getSmtpCredentials());
    }

    @Test
    @DisplayName("Should calculate delay correctly for future scheduled time")
    void shouldCalculateDelayForFutureTime() {
        // Given
        LocalDateTime futureTime = LocalDateTime.now(ZoneId.of("UTC")).plusHours(2);

        // When
        long delay = creationService.calculateDelayInSeconds(futureTime);

        // Then
        assertThat(delay).isGreaterThan(7000); // ~2 hours = 7200 seconds
        assertThat(delay).isLessThan(7300);
    }

    @Test
    @DisplayName("Should return 0 delay for past scheduled time")
    void shouldReturn0DelayForPastTime() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now(ZoneId.of("UTC")).minusHours(1);

        // When
        long delay = creationService.calculateDelayInSeconds(pastTime);

        // Then
        assertThat(delay).isEqualTo(0);
    }

    private EmailMessage createOriginalEmail() {
        EmailMessage email = new EmailMessage();
        email.setMessageId("orig-msg-123");
        email.setRecipientEmail("john@example.com");
        email.setRecipientName("John");
        email.setSentAt(LocalDateTime.now(ZoneId.of("UTC")));
        email.setUser(new User());
        email.setCampaign(new Campaign());
        email.setSmtpCredentials(new SmtpCredentials());
        return email;
    }
}
