package dev.marko.EmailSender.exception;

import dev.marko.EmailSender.entities.SmtpType;

public class ScannerNotSupportedException extends RuntimeException{
    public ScannerNotSupportedException(SmtpType type){
        super("No scanner found for type: " + type);
    }
}
