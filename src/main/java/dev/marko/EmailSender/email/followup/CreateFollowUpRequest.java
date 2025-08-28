package dev.marko.EmailSender.email.followup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

@Data
public class CreateFollowUpRequest {

    @NotNull(message = "Delay days can't be blank")
    @NumberFormat
    private Integer delayDays;
    @NotBlank(message = "Message can't be blank")
    private String message;
    @NotNull(message = "Template order can't be blank")
    @NumberFormat
    private Integer templateOrder;

}