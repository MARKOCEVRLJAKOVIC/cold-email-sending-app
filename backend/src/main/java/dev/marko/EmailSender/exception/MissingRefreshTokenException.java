package dev.marko.EmailSender.exception;

public class MissingRefreshTokenException extends IllegalArgumentException {

    public MissingRefreshTokenException(){
        super("Missing refresh token");
    }

    public MissingRefreshTokenException(String message) {
        super(message);
    }
}
