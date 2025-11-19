package dev.marko.EmailSender.auth;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException() {
        super("Token has expired");
    }
}
