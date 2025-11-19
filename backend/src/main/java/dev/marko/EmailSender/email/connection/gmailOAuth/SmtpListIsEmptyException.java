package dev.marko.EmailSender.email.connection.gmailOAuth;

public class SmtpListIsEmptyException extends RuntimeException{

    public SmtpListIsEmptyException(){
        super("Smtp list is empty");
    }
    public SmtpListIsEmptyException(String message){
        super(message);
    }


}
