package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.exception.ResourceNotFoundException;

public class FollowUpNotFoundException extends ResourceNotFoundException {

    public FollowUpNotFoundException() {
        super("Follow up message not found");
    }

    public FollowUpNotFoundException(String message) {
        super(message);
    }

}
