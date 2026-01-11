package dev.marko.EmailSender.exception;

public class InvalidPrincipalException extends UnauthorizedException{

    public InvalidPrincipalException(){
        super("Invalid authentication principal");
    }
    public InvalidPrincipalException(String message){
        super(message);
    }
}
