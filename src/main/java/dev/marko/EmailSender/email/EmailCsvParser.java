package dev.marko.EmailSender.email;

import dev.marko.EmailSender.dtos.EmailRecipientDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailCsvParser {
    public List<EmailRecipientDto> parse(MultipartFile file)  {
        List<EmailRecipientDto> recipients = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirst = true;

            while ((line = reader.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;

                var dto = new EmailRecipientDto();
                dto.setEmail(tokens[0].trim());
                dto.setName(tokens[1].trim());
                recipients.add(dto);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }

        return recipients;

    }
}
