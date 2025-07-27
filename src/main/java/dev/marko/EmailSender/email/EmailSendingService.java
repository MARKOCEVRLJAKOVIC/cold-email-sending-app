package dev.marko.EmailSender.email;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailSendingService {

    public void sendEmails(SmtpCredentials smtpCredentials, EmailMessage email){

        Properties properties = new Properties();

    }


}
