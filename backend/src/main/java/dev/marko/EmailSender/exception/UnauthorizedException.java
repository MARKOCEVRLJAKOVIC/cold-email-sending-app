package dev.marko.EmailSender.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(){
        super("Access denied");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

}
