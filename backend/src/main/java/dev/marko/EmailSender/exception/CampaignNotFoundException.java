package dev.marko.EmailSender.exception;

public class CampaignNotFoundException extends ResourceNotFoundException{

    public CampaignNotFoundException() {
        super("Campaign not found");
    }

    public CampaignNotFoundException(String message) {
        super(message);
    }

}
