package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUpQueryService Tests")
public class FollowUpQueryServiceTest {

    @Mock
    private EmailMessageRepository emailMessageRepository;

    @InjectMocks
    private FollowUpQueryService queryService;

    @Test
    @DisplayName("Should return only sent emails with campaign and no replies")
    void shouldReturnOnlyEligibleEmails() {
        // Given
        EmailMessage sentWithCampaign = createEmailMessage(Status.SENT, true, true);
        EmailMessage sentWithoutCampaign = createEmailMessage(Status.SENT, false, true);
        EmailMessage pendingWithCampaign = createEmailMessage(Status.PENDING, true, true);
        EmailMessage sentWithoutSentAt = createEmailMessage(Status.SENT, true, false);

        when(emailMessageRepository.findSentWithoutReplyOrFollowUp())
                .thenReturn(List.of(
                        sentWithCampaign,
                        sentWithoutCampaign,
                        pendingWithCampaign,
                        sentWithoutSentAt
                ));

        // When
        List<EmailMessage> result = queryService.getEligibleOriginalEmails();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sentWithCampaign);
        verify(emailMessageRepository).findSentWithoutReplyOrFollowUp();
    }

    @Test
    @DisplayName("Should return empty list when no eligible emails")
    void shouldReturnEmptyListWhenNoEligibleEmails() {
        // Given
        when(emailMessageRepository.findSentWithoutReplyOrFollowUp())
                .thenReturn(List.of());

        // When
        List<EmailMessage> result = queryService.getEligibleOriginalEmails();

        // Then
        assertThat(result).isEmpty();
    }

    private EmailMessage createEmailMessage(Status status, boolean hasCampaign, boolean hasSentAt) {
        EmailMessage email = new EmailMessage();
        email.setStatus(status);
        email.setCampaign(hasCampaign ? new Campaign() : null);
        email.setSentAt(hasSentAt ? LocalDateTime.now() : null);
        return email;
    }
}