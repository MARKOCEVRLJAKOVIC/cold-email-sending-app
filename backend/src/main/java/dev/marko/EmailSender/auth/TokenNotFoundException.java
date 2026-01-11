package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.exception.ResourceNotFoundException;

public class TokenNotFoundException extends ResourceNotFoundException {

    public TokenNotFoundException(){
        super("Token not found");
    }

    public TokenNotFoundException(String message){
        super(message);
    }

}
