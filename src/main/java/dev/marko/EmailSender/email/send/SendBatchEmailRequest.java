package dev.marko.EmailSender.email.send;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Data
public class SendBatchEmailRequest {
    @NotNull
    private MultipartFile file;

    private LocalDateTime scheduledAt;

    @NotNull
    private Long templateId;

    @NotEmpty
    private List<Long> smtpIds;

    private Long campaignId;
}
