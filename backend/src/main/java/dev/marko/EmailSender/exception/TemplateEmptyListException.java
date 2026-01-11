package dev.marko.EmailSender.exception;

public class TemplateEmptyListException extends BadRequestException{
    public TemplateEmptyListException() {
        super("List is empty");
    }

    public TemplateEmptyListException(String message) {
        super(message);
    }
}
