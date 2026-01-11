package dev.marko.EmailSender.exception;

public class UserNotConfirmedException extends UnauthorizedException {

    public UserNotConfirmedException() {
        super("User not confirmed");
    }

    public UserNotConfirmedException(String message) {
        super(message);
    }
}
