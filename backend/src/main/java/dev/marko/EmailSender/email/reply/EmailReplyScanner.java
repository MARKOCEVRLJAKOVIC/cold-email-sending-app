package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.entities.SmtpType;

public interface EmailReplyScanner {
    SmtpType supports();
    void checkReplies();
}
