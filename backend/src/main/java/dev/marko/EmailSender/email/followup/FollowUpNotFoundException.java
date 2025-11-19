package dev.marko.EmailSender.email.followup;

public class FollowUpNotFoundException extends RuntimeException {

    public FollowUpNotFoundException() {
        super("Follow up message not found");
    }

    public FollowUpNotFoundException(String message) {
        super(message);
    }

}
