package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.send.batch.EmailMessageCreationService;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailMessageCreationServiceTest {

    @Mock private EmailPreparationService preparationService;
    @Mock private EmailMessageRepository emailMessageRepository;

    @InjectMocks EmailMessageCreationService emailMessageCreationService;

    EmailRecipientDto emailRecipientDto1;
    EmailRecipientDto emailRecipientDto2;
    List<EmailRecipientDto> recipients;

    SmtpCredentials smtp1;
    SmtpCredentials smtp2;
    List<SmtpCredentials> smtpList;

    EmailTemplate template;

    Campaign campaign;


    @BeforeEach
    public void setup(){

        emailRecipientDto1 = new EmailRecipientDto("example@email.com", "John");
        emailRecipientDto2 = new EmailRecipientDto("example2@email.com", "Mark");
        recipients = List.of(emailRecipientDto1, emailRecipientDto2);

        smtp1 = new SmtpCredentials();
        smtp2 = new SmtpCredentials();

        smtpList = List.of(smtp1, smtp2);
        template = new EmailTemplate();

        campaign = new Campaign();
        campaign.setTimezone("Europe/Belgrade");

    }

    @Test
    void prepareAndSaveEmails_ShouldSaveEmailsAndReturnEmailMessageList(){

        template.setMessage("Hi {{name}}, hope you are having good day.");
        String expectedMessageText = "Hi John, hope you are having good day.";

        when(preparationService.generateMessageText(template.getMessage(), emailRecipientDto1.getName())).thenReturn(expectedMessageText);

        var result = emailMessageCreationService.prepareAndSaveEmails(recipients, smtpList, new User(), template, campaign, LocalDateTime.now());

        assertEquals(2, result.size());
        assertEquals(smtp1, result.getFirst().getSmtpCredentials());
        assertEquals(smtp2, result.get(1).getSmtpCredentials());
        assertEquals(expectedMessageText, result.getFirst().getSentMessage());

        verify(emailMessageRepository, times(2)).save(any());

    }

}
