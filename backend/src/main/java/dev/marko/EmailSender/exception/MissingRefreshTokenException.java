package dev.marko.EmailSender.exception;

public class MissingRefreshTokenException extends BadRequestException {

    public MissingRefreshTokenException(){
        super("Missing refresh token");
    }

    public MissingRefreshTokenException(String message) {
        super(message);
    }
}
