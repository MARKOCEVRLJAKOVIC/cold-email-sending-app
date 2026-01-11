package dev.marko.EmailSender.exception;

import dev.marko.EmailSender.entities.SmtpType;

public class ScannerNotSupportedException extends BadRequestException{
    public ScannerNotSupportedException(SmtpType type){
        super("No scanner found for type: " + type);
    }
}
