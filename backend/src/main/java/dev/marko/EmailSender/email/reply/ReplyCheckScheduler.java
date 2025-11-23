package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.exception.ScannerNotSupportedException;
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
                var scanner = delegator.getScanner(type);
                if(scanner != null) {
                    scanner.checkReplies();
                }
            } catch (ScannerNotSupportedException ignored) {
                // no scanner of this type, skip
            }
        }
    }
}