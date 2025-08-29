package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@AllArgsConstructor
@Service
public class EmailSenderDelegator implements EmailSender{

    private final GmailSmtpSender gmailSmtpSender;
    private final SmtpEmailSender smtpEmailSender;
    private final EmailMessageRepository emailMessageRepository;

    @Override
    public void sendEmails(EmailMessage  emailMessage) throws MessagingException {

        if(emailMessage.getSmtpCredentials().isGmailOauth()){
            gmailSmtpSender.sendEmails(emailMessage);
        }
        else {
            smtpEmailSender.sendEmails(emailMessage);
        }
        emailMessageRepository.save(emailMessage);

    }
}
