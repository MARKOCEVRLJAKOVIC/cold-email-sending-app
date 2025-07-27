package dev.marko.EmailSender.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailPreparationService {

    public String generateMessageText(String templateText, String recipientName) {
        return templateText.replace("{{name}}", recipientName);
    }

}
