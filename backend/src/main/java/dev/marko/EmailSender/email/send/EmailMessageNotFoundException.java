package dev.marko.EmailSender.email.send;

public class EmailMessageNotFoundException extends RuntimeException {

    public EmailMessageNotFoundException(){
        super("Email message not found");
    }
    public EmailMessageNotFoundException(String message){
        super(message);
    }

}
