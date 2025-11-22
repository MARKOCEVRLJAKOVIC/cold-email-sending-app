package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.entities.SmtpType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyCheckScheduler {

    private final EmailReplyDelegator delegator;

    @Scheduled(fixedDelay = 300_000) // every 5 minutes
    public void scanAll() {
        for (SmtpType type : SmtpType.values()) {
            try {
                delegator.getScanner(type).checkReplies();
            } catch (IllegalArgumentException ignored) {
                // no scanner of this type, skip
            }
        }
    }
}