package dev.marko.EmailSender.exception;

public class TemplateNotFoundException extends RuntimeException{
    public TemplateNotFoundException() {
        super("Template not found");
    }
    public TemplateNotFoundException(String message) {
        super(message);
    }

}
