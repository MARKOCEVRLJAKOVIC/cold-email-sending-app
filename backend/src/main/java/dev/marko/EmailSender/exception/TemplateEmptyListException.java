package dev.marko.EmailSender.exception;

public class TemplateEmptyListException extends RuntimeException{
    public TemplateEmptyListException() {
        super("List is empty");
    }

    public TemplateEmptyListException(String message) {
        super(message);
    }
}
