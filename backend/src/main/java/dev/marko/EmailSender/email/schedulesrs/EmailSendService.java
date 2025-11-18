package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.email.send.EmailSender;
import dev.marko.EmailSender.entities.EmailMessage;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService {

    private final EmailSender emailSender;
    private final EmailStatusService statusService;

    public void sendAndPersist(EmailMessage email) {
        try {
            emailSender.sendEmails(email);
            statusService.markSent(email);

        } catch (MessagingException e) {
            statusService.markFailed(email, e);

        } catch (Exception e) {
            statusService.markFailed(email, e);
        }
    }
}