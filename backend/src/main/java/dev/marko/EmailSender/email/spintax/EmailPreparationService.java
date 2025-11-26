package dev.marko.EmailSender.email.spintax;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailPreparationService {

    private final SpintaxProcessor spinTextProcessor;

    public String generateMessageText(String templateText, String recipientName) {

        String name = (recipientName == null || recipientName.isBlank()) ? "" : recipientName.trim();
        String withName = templateText.replace("{{name}}", name);
        return spinTextProcessor.process(withName);

    }
}


