package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
public class FollowUpDto {

    private Long id;
    private Integer delayDays;
    private String message;
    private Long userId;
    private Long campaignId;

}
