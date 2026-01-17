package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.email.schedulers.EmailSchedulingService;
import dev.marko.EmailSender.email.send.batch.BatchSchedulingService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class BatchSchedulingServiceTest {

    @Mock private EmailSchedulingService emailSchedulingService;

    @InjectMocks BatchSchedulingService batchSchedulingService;

    List<EmailMessage> allMessages;
    Campaign campaign;
    private static final int DEFAULT_DELAY = 15;

    @BeforeEach
    void setup(){

        allMessages = List.of(new EmailMessage(), new EmailMessage());
        when(emailSchedulingService.getDefaultDelay()).thenReturn(DEFAULT_DELAY);

        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setTimezone("Europe/Belgrade");

    }

    @Test
    void scheduleEmails_ShouldScheduleEmailsOneByOne(){

        LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 1, 10, 0);

        batchSchedulingService.scheduleEmails(scheduledAt, allMessages, campaign);

        // Verify first email is scheduled with 0 additional delay
        verify(emailSchedulingService).scheduleSingle(eq(allMessages.get(0)), eq(scheduledAt), eq(0L));
        // Verify second email is scheduled with an interval delay
        verify(emailSchedulingService).scheduleSingle(eq(allMessages.get(1)), eq(scheduledAt), eq((long)DEFAULT_DELAY));
    }

    @Test
    void scheduleEmails_ShouldScheduleBatchEmailsWhenScheduledAtIsNull(){

        batchSchedulingService.scheduleEmails(null, allMessages, campaign);
        verify(emailSchedulingService).scheduleBatch(allMessages, emailSchedulingService.getDefaultDelay());

    }
}
