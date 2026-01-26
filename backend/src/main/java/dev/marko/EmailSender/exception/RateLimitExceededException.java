package dev.marko.EmailSender.exception;

public class RateLimitExceededException extends BadRequestException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}