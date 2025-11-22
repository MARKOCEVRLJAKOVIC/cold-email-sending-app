package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.entities.SmtpType;
import org.springframework.scheduling.annotation.Scheduled;

public interface EmailReplyScanner {
    SmtpType supports();
    void checkReplies();
}
