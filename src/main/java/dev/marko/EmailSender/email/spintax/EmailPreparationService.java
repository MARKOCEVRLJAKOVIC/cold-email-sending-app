package dev.marko.EmailSender.email.spintax;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailPreparationService {

    private final SpintaxProcessor spinTextProcessor;

    public String generateMessageText(String templateText, String recipientName) {
        String withName = templateText.replace("{{name}}", recipientName);
        return spinTextProcessor.process(withName);
    }
}
