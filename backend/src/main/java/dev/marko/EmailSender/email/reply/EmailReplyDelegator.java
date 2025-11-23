package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.exception.ScannerNotSupportedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailReplyDelegator {

    private final Map<SmtpType, EmailReplyScanner> scanners;

    public EmailReplyDelegator(List<EmailReplyScanner> scanners){
        this.scanners = scanners.stream()
                .collect(Collectors.toMap(EmailReplyScanner::supports, s -> s ));
    }

    public EmailReplyScanner getScanner(SmtpType type) {
        EmailReplyScanner scanner = scanners.get(type);
        if (scanner == null) {
            throw new ScannerNotSupportedException(type);
        }
        return scanner;
    }

}
