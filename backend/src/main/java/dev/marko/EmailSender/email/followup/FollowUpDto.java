package dev.marko.EmailSender.email.followup;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FollowUpDto {

    private Long id;
    private Integer delayDays;
    private String message;
    private Long userId;
    private Long campaignId;

}
