package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.schedulers.EmailSchedulingService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.security.SensitiveDataMasker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUpSchedulerService Tests - Integration")
public class FollowUpSchedulerServiceTest {

    @Mock
    private FollowUpQueryService queryService;

    @Mock
    private FollowUpEligibilityService eligibilityService;

    @Mock
    private FollowUpCreationService creationService;

    @Mock
    private EmailSchedulingService schedulingService;

    @Mock
    private SensitiveDataMasker sensitiveDataMasker;

    @InjectMocks
    private FollowUpSchedulerService schedulerService;

    @Test
    @DisplayName("Should process all eligible emails and schedule follow-ups")
    void shouldProcessAllEligibleEmails() {
        // Given
        EmailMessage email1 = createEmail(1L, "user1@test.com");
        EmailMessage email2 = createEmail(2L, "user2@test.com");

        FollowUpTemplate template1 = createTemplate(1L, 3);
        FollowUpTemplate template2 = createTemplate(2L, 5);

        EmailMessage followUp1 = createFollowUpEmail(email1, template1);
        EmailMessage followUp2 = createFollowUpEmail(email2, template2);

        when(queryService.getEligibleOriginalEmails())
                .thenReturn(List.of(email1, email2));
        when(eligibilityService.findEligibleTemplate(email1))
                .thenReturn(template1);
        when(eligibilityService.findEligibleTemplate(email2))
                .thenReturn(template2);
        when(creationService.createFollowUp(email1, template1))
                .thenReturn(followUp1);
        when(creationService.createFollowUp(email2, template2))
                .thenReturn(followUp2);
        when(creationService.calculateDelayInSeconds(any()))
                .thenReturn(3600L);
        when(sensitiveDataMasker.maskEmail(any()))
                .thenReturn("us***@test.com");

        // When
        schedulerService.scheduleFollowUps();

        // Then
        verify(queryService).getEligibleOriginalEmails();
        verify(eligibilityService, times(2)).findEligibleTemplate(any());
        verify(creationService, times(2)).createFollowUp(any(), any());
        verify(schedulingService, times(2)).scheduleFollowUp(any(), eq(3600L));
    }

    @Test
    @DisplayName("Should skip emails with no eligible template")
    void shouldSkipEmailsWithNoTemplate() {
        // Given
        EmailMessage email1 = createEmail(1L, "user1@test.com");
        EmailMessage email2 = createEmail(2L, "user2@test.com");

        when(queryService.getEligibleOriginalEmails())
                .thenReturn(List.of(email1, email2));
        when(eligibilityService.findEligibleTemplate(email1))
                .thenReturn(null); // No template
        when(eligibilityService.findEligibleTemplate(email2))
                .thenReturn(null); // No template

        // When
        schedulerService.scheduleFollowUps();

        // Then
        verify(creationService, never()).createFollowUp(any(), any());
        verify(schedulingService, never()).scheduleFollowUp(any(), anyLong());
    }

    @Test
    @DisplayName("Should handle empty eligible emails list")
    void shouldHandleEmptyList() {
        // Given
        when(queryService.getEligibleOriginalEmails())
                .thenReturn(List.of());

        // When
        schedulerService.scheduleFollowUps();

        // Then
        verify(eligibilityService, never()).findEligibleTemplate(any());
        verify(creationService, never()).createFollowUp(any(), any());
    }

    @Test
    @DisplayName("Should mask email addresses in logs")
    void shouldMaskEmailsInLogs() {
        // Given
        EmailMessage email = createEmail(1L, "sensitive@example.com");
        FollowUpTemplate template = createTemplate(1L, 3);
        EmailMessage followUp = createFollowUpEmail(email, template);

        when(queryService.getEligibleOriginalEmails()).thenReturn(List.of(email));
        when(eligibilityService.findEligibleTemplate(email)).thenReturn(template);
        when(creationService.createFollowUp(email, template)).thenReturn(followUp);
        when(creationService.calculateDelayInSeconds(any())).thenReturn(0L);
        when(sensitiveDataMasker.maskEmail("sensitive@example.com"))
                .thenReturn("se***@example.com");

        // When
        schedulerService.scheduleFollowUps();

        // Then
        verify(sensitiveDataMasker).maskEmail("sensitive@example.com");
    }

    private EmailMessage createEmail(Long id, String email) {
        EmailMessage msg = new EmailMessage();
        msg.setId(id);
        msg.setRecipientEmail(email);
        msg.setMessageId("msg-" + id);
        msg.setSentAt(LocalDateTime.now(ZoneId.of("UTC")));
        return msg;
    }

    private FollowUpTemplate createTemplate(Long id, int delayDays) {
        FollowUpTemplate template = new FollowUpTemplate();
        template.setId(id);
        template.setDelayDays(delayDays);
        return template;
    }

    private EmailMessage createFollowUpEmail(EmailMessage original, FollowUpTemplate template) {
        EmailMessage followUp = new EmailMessage();
        followUp.setScheduledAt(original.getSentAt().plusDays(template.getDelayDays()));
        followUp.setRecipientEmail(original.getRecipientEmail());
        return followUp;
    }
}
