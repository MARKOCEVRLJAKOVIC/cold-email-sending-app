package dev.marko.EmailSender.email.connection.gmailOAuth;

import lombok.Data;

@Data
public class GmailConnectRequest {

    private String code;
    private String senderEmail;

}