package dev.marko.EmailSender.email;

import dev.marko.EmailSender.email.gmailOAuth.GmailSmtpSender;
import dev.marko.EmailSender.entities.EmailMessage;
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

    @Override
    public void sendEmails(EmailMessage emailMessage) throws MessagingException {

        if(emailMessage.getSmtpCredentials().isGmailOauth()){
            gmailSmtpSender.sendEmails(emailMessage);
        }
        else {
            smtpEmailSender.sendEmails(emailMessage);
        }

    }
}
