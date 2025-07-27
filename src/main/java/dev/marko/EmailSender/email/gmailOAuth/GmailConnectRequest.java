package dev.marko.EmailSender.email.gmailOAuth;

import lombok.Data;

@Data
public class GmailConnectRequest {

    private String code;
    private String senderEmail;

}