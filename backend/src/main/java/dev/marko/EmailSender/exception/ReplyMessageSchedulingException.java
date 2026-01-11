package dev.marko.EmailSender.exception;

public class ReplyMessageSchedulingException extends BadRequestException{
    public ReplyMessageSchedulingException(){
        super("Failed to schedule reply message");
    }
    public ReplyMessageSchedulingException(String message){
        super(message);
    }
}
