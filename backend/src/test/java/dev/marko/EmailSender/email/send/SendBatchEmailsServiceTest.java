package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@RequiredArgsConstructor
public class SendBatchEmailsServiceTest {

    @Mock private AuthService authService;
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



    }

    @Test
    public void testSendBatchEmails() {

        String csvContent = "email1@test.com,Marko\nemail2@test.com,Marko";
        EmailRecipientDto recipientDto = new EmailRecipientDto("test@email.com", "test2@email.com");


        MultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());



        when(authService.getCurrentUser()).thenReturn(user);
        when(templateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(smtpRepository.findAllById(List.of(smtpCredentials.getId()))).thenReturn(List.of(smtpCredentials));
        when(preparationService.generateMessageText(anyString(), anyString())).thenReturn(recipientDto.getName(), recipientDto.getEmail());
        when(emailMessageMapper.toDto(any())).thenAnswer(invocation -> new EmailMessageDto());

        var result = sendBatchEmailsService.sendBatchEmails(file, LocalDateTime.now(), 1L, List.of(1L), 1L);

        verify(emailMessageRepository, times(1)).save(any());
        verify(emailSchedulingService, times(1)).scheduleSingle(any(), anyLong());

        assertEquals(1, result.size());


    }

}
