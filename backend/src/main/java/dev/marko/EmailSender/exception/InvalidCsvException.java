package dev.marko.EmailSender.exception;

import java.io.IOException;

public class InvalidCsvException extends RuntimeException{
    public InvalidCsvException(){
        super("Invalid Csv file");
    }

    public InvalidCsvException(String message){
        super(message);
    }

    public InvalidCsvException(String message, IOException e) {
        super(message, e);
    }
}
