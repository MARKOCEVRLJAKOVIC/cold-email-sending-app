package dev.marko.EmailSender.exception;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(){
        super("User not found");
    }
    public UserNotFoundException(String message){
        super(message);
    }


}

