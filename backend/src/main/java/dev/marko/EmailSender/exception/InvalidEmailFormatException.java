package dev.marko.EmailSender.exception;

public class InvalidEmailFormatException extends RuntimeException {

    public InvalidEmailFormatException(){
        super("Invalid email format.");
    }
    public InvalidEmailFormatException(String message){
        super(message);
    }

}
