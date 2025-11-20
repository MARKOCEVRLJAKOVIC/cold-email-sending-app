package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.send.batch.BatchSchedulingService;
import dev.marko.EmailSender.email.send.batch.CsvParserService;
import dev.marko.EmailSender.email.send.batch.EmailMessageCreationService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SendBatchEmailsServiceTest {

    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private TemplateRepository templateRepository;
    @Mock private SmtpRepository smtpRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private EmailMessageMapper emailMessageMapper;

    @Mock private CsvParserService csvParserService;
    @Mock private EmailMessageCreationService emailMessageCreationService;
    @Mock private BatchSchedulingService batchSchedulingService;

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

        MultipartFile file = getFile("""
                email,name
                email1@test.com,Marko
                email2@test.com,Marko
                """);

        when(templateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(smtpRepository.findAllById(List.of(1L))).thenReturn(List.of(smtpCredentials));

        when(csvParserService.parseCsv(any())).thenReturn(List.of(
                new EmailRecipientDto("email1@test.com", "Marko"),
                new EmailRecipientDto("email2@test.com", "Marko")
        ));

        when(emailMessageCreationService.prepareAndSaveEmails(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(new EmailMessage(), new EmailMessage()));

        when(emailMessageMapper.toDto(any())).thenReturn(new EmailMessageDto());

        var result = sendBatchEmailsService.sendBatchEmails(
                file,
                LocalDateTime.now(),
                template.getId(),
                List.of(smtpCredentials.getId()),
                campaign.getId()
        );

        assertEquals(2, result.size());
    }

    @Test
    public void testSendBatchEmails_ThrowsWhenTemplateNotFound() {

        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());

        MultipartFile file = getFile("email,name\nemail@email.com");

        assertThrows(TemplateNotFoundException.class, () ->
                sendBatchEmailsService.sendBatchEmails(
                        file,
                        LocalDateTime.now(),
                        999L,
                        List.of(smtpCredentials.getId()),
                        campaign.getId()
                )
        );
    }

    @Test
    public void testSmtpRotation() {

        when(templateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(campaignRepository.findById(anyLong())).thenReturn(Optional.of(campaign));

        when(smtpRepository.findAllById(any())).thenReturn(List.of(
                createSmtp(1L, "smtp1@email.com"),
                createSmtp(2L, "smtp2@email.com"),
                createSmtp(3L, "smtp3@email.com"),
                createSmtp(4L, "smtp4@email.com"),
                createSmtp(5L, "smtp5@email.com")
        ));

        MultipartFile file = getFile("""
                email,name
                a@test.com,a
                b@test.com,b
                c@test.com,c
                d@test.com,d
                e@test.com,e
                f@test.com,f
                g@test.com,g
                """);

        List<EmailMessage> savedMessages = new ArrayList<>();

        when(emailMessageCreationService.prepareAndSaveEmails(
                any(), any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            // simulate saving 7 messages
            List<EmailMessage> emails = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                EmailMessage msg = new EmailMessage();
                msg.setSmtpCredentials(
                        smtpRepository.findAllById(any()).get(i % 5)
                );
                savedMessages.add(msg);
                emails.add(msg);
            }
            return emails;
        });

        sendBatchEmailsService.sendBatchEmails(
                file,
                LocalDateTime.now(),
                template.getId(),
                List.of(1L, 2L, 3L, 4L, 5L),
                campaign.getId()
        );

        assertEquals(7, savedMessages.size());

        assertEquals("smtp1@email.com", savedMessages.get(0).getSmtpCredentials().getEmail());
        assertEquals("smtp2@email.com", savedMessages.get(1).getSmtpCredentials().getEmail());
        assertEquals("smtp3@email.com", savedMessages.get(2).getSmtpCredentials().getEmail());
        assertEquals("smtp4@email.com", savedMessages.get(3).getSmtpCredentials().getEmail());
        assertEquals("smtp5@email.com", savedMessages.get(4).getSmtpCredentials().getEmail());
        assertEquals("smtp1@email.com", savedMessages.get(5).getSmtpCredentials().getEmail());
        assertEquals("smtp2@email.com", savedMessages.get(6).getSmtpCredentials().getEmail());
    }

    // helpers

    private SmtpCredentials createSmtp(Long id, String email) {
        SmtpCredentials smtp = new SmtpCredentials();
        smtp.setId(id);
        smtp.setEmail(email);
        smtp.setUser(user);
        return smtp;
    }

    private MultipartFile getFile(String content) {
        return new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes());
    }
}
