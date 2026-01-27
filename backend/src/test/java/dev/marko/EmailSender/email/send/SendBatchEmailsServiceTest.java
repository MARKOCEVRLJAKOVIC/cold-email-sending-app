package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.send.batch.BatchSchedulingService;
import dev.marko.EmailSender.email.send.batch.CsvParserService;
import dev.marko.EmailSender.email.send.batch.EmailMessageCreationService;
import dev.marko.EmailSender.email.send.batch.SendBatchEmailsService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.ratelimit.UserRateLimiter;
import dev.marko.EmailSender.repositories.CampaignRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendBatchEmailsServiceTest {

    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private TemplateRepository templateRepository;
    @Mock private SmtpRepository smtpRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private EmailMessageMapper emailMessageMapper;

    @Mock private CsvParserService csvParserService;
    @Mock private EmailMessageCreationService emailMessageCreationService;
    @Mock private BatchSchedulingService batchSchedulingService;
    @Mock private UserRateLimiter userRateLimiter;

    @InjectMocks
    private SendBatchEmailsService sendBatchEmailsService;

    private User user;
    private EmailTemplate template;
    private Campaign campaign;
    private SmtpCredentials smtpCredentials;
    private EmailMessage emailMessage1;
    private EmailMessage emailMessage2;

    private final Long VALID_ID = 1L;
    private final Long INVALID_ID = 99L;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(VALID_ID);

        template = new EmailTemplate();
        template.setId(VALID_ID);
        template.setUser(user);
        template.setMessage("Hello {{name}}");

        campaign = new Campaign();
        campaign.setId(VALID_ID);
        campaign.setUser(user);
        campaign.setTimezone("Europe/Belgrade");

        smtpCredentials = new SmtpCredentials();
        smtpCredentials.setId(VALID_ID);
        smtpCredentials.setUser(user);

        emailMessage1 = new EmailMessage();
        emailMessage2 = new EmailMessage();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        doNothing().when(userRateLimiter).consumeBatchOrThrow(any(User.class));
    }

    @Test
    void testSendBatchEmails() {

        doNothing().when(userRateLimiter)
                .consumeEmailsOrThrow(any(User.class), anyInt());

        MultipartFile file = getFile("""
                name,email
                Marko,email1@test.com
                Marko,email2@test.com
                """);

        when(templateRepository.findByIdAndUserId(template.getId(), user.getId()))
                .thenReturn(Optional.of(template));
        when(campaignRepository.findByIdAndUserId(campaign.getId(), user.getId()))
                .thenReturn(Optional.of(campaign));
        when(smtpRepository.findAllById(List.of(VALID_ID)))
                .thenReturn(List.of(smtpCredentials));

        when(csvParserService.parseCsv(any()))
                .thenReturn(List.of(
                        new EmailRecipientDto("Marko", "email1@test.com"),
                        new EmailRecipientDto("Marko", "email2@test.com")
                ));

        when(emailMessageCreationService.prepareAndSaveEmails(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(emailMessage1, emailMessage2));

        when(emailMessageMapper.toDto(any()))
                .thenReturn(new EmailMessageDto());

        var result = sendBatchEmailsService.sendBatchEmails(
                file,
                LocalDateTime.now(),
                template.getId(),
                List.of(smtpCredentials.getId()),
                campaign.getId()
        );

        assertEquals(2, result.size());

        verify(csvParserService).parseCsv(file);
        verify(emailMessageCreationService)
                .prepareAndSaveEmails(any(), any(), any(), any(), any(), any());
        verify(batchSchedulingService)
                .scheduleEmails(any(), eq(List.of(emailMessage1, emailMessage2)), any());
        verify(emailMessageMapper, times(2)).toDto(any());
    }

    @Test
    void testSendBatchEmails_ThrowsWhenTemplateNotFound() {

        when(templateRepository.findByIdAndUserId(INVALID_ID, user.getId()))
                .thenReturn(Optional.empty());

        MultipartFile file = getFile("email,name\nemail@email.com");

        assertThrows(TemplateNotFoundException.class, () ->
                sendBatchEmailsService.sendBatchEmails(
                        file,
                        LocalDateTime.now(),
                        INVALID_ID,
                        List.of(smtpCredentials.getId()),
                        campaign.getId()
                )
        );
    }

    @Test
    void testSmtpRotation() {

        doNothing().when(userRateLimiter)
                .consumeEmailsOrThrow(any(User.class), anyInt());

        when(templateRepository.findByIdAndUserId(template.getId(), user.getId()))
                .thenReturn(Optional.of(template));
        when(campaignRepository.findByIdAndUserId(VALID_ID, user.getId()))
                .thenReturn(Optional.of(campaign));

        List<SmtpCredentials> smtpList = List.of(
                createSmtp(1L, "smtp1@email.com"),
                createSmtp(2L, "smtp2@email.com"),
                createSmtp(3L, "smtp3@email.com"),
                createSmtp(4L, "smtp4@email.com"),
                createSmtp(5L, "smtp5@email.com")
        );

        when(smtpRepository.findAllById(any()))
                .thenReturn(smtpList);

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
            List<EmailMessage> emails = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                EmailMessage msg = new EmailMessage();
                msg.setSmtpCredentials(smtpList.get(i % smtpList.size()));
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
        return new MockMultipartFile(
                "file", "test.csv", "text/csv", content.getBytes()
        );
    }
}
