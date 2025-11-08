package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailTemplate;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor
public class SendBatchEmailServiceTest {

    private final SendBatchEmailsService sendBatchEmailsService;
    private final AuthService authService;

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

        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "email1@test.com,John\nemail2@test.com,Jane".getBytes());



        when(authService.getCurrentUser()).thenReturn(user);


        var sendBatchEmails =  sendBatchEmailsService.sendBatchEmails(file, LocalDateTime.now(), template.getId(), List.of(smtpCredentials.getId()), campaign.getId());




    }

}
