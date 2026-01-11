package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.exception.UnauthorizedException;

public class TokenExpiredException extends UnauthorizedException {

    public TokenExpiredException() {
        super("Token has expired");
    }
}
