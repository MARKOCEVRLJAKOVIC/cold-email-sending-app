package dev.marko.EmailSender.exception;

public class InvalidPrincipalException extends RuntimeException{

    public InvalidPrincipalException(){
        super("Invalid authentication principal");
    }
    public InvalidPrincipalException(String message){
        super(message);
    }
}
