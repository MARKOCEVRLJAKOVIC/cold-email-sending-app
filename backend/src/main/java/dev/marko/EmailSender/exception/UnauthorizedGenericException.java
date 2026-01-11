package dev.marko.EmailSender.exception;

public class UnauthorizedGenericException extends UnauthorizedException {

    public UnauthorizedGenericException(){
        super("Access denied");
    }

    public UnauthorizedGenericException(String message) {
        super(message);
    }

}
