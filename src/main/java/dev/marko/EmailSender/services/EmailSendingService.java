package dev.marko.EmailSender.services;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Smtp;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailSendingService {

    public void sendEmails(Smtp smtp, EmailMessage email){

        Properties properties = new Properties();

    }


}
