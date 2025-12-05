package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.send.batch.BatchSchedulingService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BatchSchedulingServiceTest {

    @Mock private EmailSchedulingService emailSchedulingService;

    @InjectMocks BatchSchedulingService batchSchedulingService;

    List<EmailMessage> allMessages;
    Campaign campaign;

    @BeforeEach
    void setup(){

        allMessages = List.of(new EmailMessage(), new EmailMessage());
        when(emailSchedulingService.getDefaultDelay()).thenReturn(15);

        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setTimezone("Europe/Belgrade");

    }

    @Test
    void scheduleEmails_ShouldScheduleEmailsOneByOne(){


        LocalDateTime fixedNow = LocalDateTime.of(2025, 1, 1, 10, 0);

        LocalDateTime scheduledAt = fixedNow.plusSeconds(30);

        batchSchedulingService.scheduleEmails(scheduledAt, allMessages, campaign);
        verify(emailSchedulingService).scheduleSingle(allMessages.getFirst(), emailSchedulingService.getDefaultDelay(), LocalDateTime.now());

    }

    @Test
    void scheduleEmails_ShouldScheduleBatchEmailsWhenScheduledAtIsNull(){

        batchSchedulingService.scheduleEmails(null, allMessages, campaign);
        verify(emailSchedulingService).scheduleBatch(allMessages, emailSchedulingService.getDefaultDelay());

    }
}
