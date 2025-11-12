package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SendBatchEmailsServiceTest {

    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private TemplateRepository templateRepository;
    @Mock private SmtpRepository smtpRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private EmailMessageRepository emailMessageRepository;
    @Mock private EmailPreparationService preparationService;
    @Mock private EmailSchedulingService emailSchedulingService;
    @Mock private EmailMessageMapper emailMessageMapper;

    @InjectMocks
    private SendBatchEmailsService sendBatchEmailsService;

    private User user;
    private EmailTemplate template;
    private Campaign campaign;
    private SmtpCredentials smtpCredentials;

    @BeforeEach
    public void setup() {

        user = new User();
        user.setId(1L);

        template = new EmailTemplate();
        template.setId(1L);
        template.setUser(user);
        template.setMessage("Hello {{name}}");

        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setUser(user);

        smtpCredentials = new SmtpCredentials();
        smtpCredentials.setId(1L);
        smtpCredentials.setUser(user);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);


    }

    @Test
    public void testSendBatchEmails() {

        EmailRecipientDto recipientDto = new EmailRecipientDto("test@email.com", "test2@email.com");

        MultipartFile file = getFile("email,name\n" +
                "email1@test.com,Marko\n" +
                "email2@test.com,Marko");

        when(templateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(smtpRepository.findAllById(List.of(smtpCredentials.getId()))).thenReturn(List.of(smtpCredentials));
        when(preparationService.generateMessageText(anyString(), anyString())).thenReturn(recipientDto.getName(), recipientDto.getEmail());
        when(emailMessageMapper.toDto(any())).thenAnswer(invocation -> new EmailMessageDto());

        var result = sendBatchEmailsService.sendBatchEmails(file, LocalDateTime.now(), 1L, List.of(1L), 1L);

        verify(emailMessageRepository, times(2)).save(any());
        verify(emailSchedulingService, times(2)).scheduleSingle(any(), anyLong());

        assertEquals(2, result.size());

    }

    @Test
    public void testSendBatchEmails_ThrowsWhenTemplateNotFound() {


        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());

        MultipartFile file = getFile("email,name\nemail@email.com\nmarko");

        assertThrows(TemplateNotFoundException.class, () -> {
            sendBatchEmailsService.sendBatchEmails(file, LocalDateTime.now(), 999L, List.of(smtpCredentials.getId()), campaign.getId());
        });

    }

    @Test
    public void testSmtpRotation() {
        when(templateRepository.findById(template.getId())).thenReturn(Optional.of(template));

        when(smtpRepository.findAllById(any())).thenReturn(List.of(
                createSmtp(1L, "smtp1@email.com"),
                createSmtp(2L, "smtp2@email.com"),
                createSmtp(3L, "smtp3@email.com"),
                createSmtp(4L, "smtp4@email.com"),
                createSmtp(5L, "smtp5@email.com")
        ));

        MultipartFile file = getFile(
                "email,name\n" +
                        "a@test.com,a\n" +
                        "b@test.com,b\n" +
                        "c@test.com,c\n" +
                        "d@test.com,d\n" +
                        "e@test.com,e\n" +
                        "f@test.com,f\n" +
                        "g@test.com,g");

        List<EmailMessage> savedMessages = new ArrayList<>();
        when(emailMessageRepository.save(any())).thenAnswer(invocation -> {
            EmailMessage emailMessage = invocation.getArgument(0);
            savedMessages.add(emailMessage);
            return emailMessage;
        });

        sendBatchEmailsService.sendBatchEmails(file, LocalDateTime.now(), template.getId()
                , List.of(1L, 2L, 3L, 4L, 5L), campaign.getId());

        assertEquals(7, savedMessages.size());

        assertEquals("smtp1@email.com", savedMessages.get(0).getSmtpCredentials().getEmail());
        assertEquals("smtp2@email.com", savedMessages.get(1).getSmtpCredentials().getEmail());
        assertEquals("smtp3@email.com", savedMessages.get(2).getSmtpCredentials().getEmail());
        assertEquals("smtp4@email.com", savedMessages.get(3).getSmtpCredentials().getEmail());
        assertEquals("smtp5@email.com", savedMessages.get(4).getSmtpCredentials().getEmail());
        assertEquals("smtp1@email.com", savedMessages.get(5).getSmtpCredentials().getEmail());
        assertEquals("smtp2@email.com", savedMessages.get(6).getSmtpCredentials().getEmail());



    }



    // helper methods

    private SmtpCredentials createSmtp(Long id, String email) {
        SmtpCredentials smtp = new SmtpCredentials();
        smtp.setId(id);
        smtp.setEmail(email);
        smtp.setUser(user);
        return smtp;
    }

    public MultipartFile getFile(String content) {
        return new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes());
    }

}
