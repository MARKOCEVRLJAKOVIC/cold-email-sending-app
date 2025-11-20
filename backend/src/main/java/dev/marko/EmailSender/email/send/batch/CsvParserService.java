package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.dtos.EmailRecipientDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CsvParserService {

    public List<EmailRecipientDto> parseCsv(MultipartFile file)  {
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


                String email = tokens[0].trim();
                String name = tokens.length > 1 ? tokens[1].trim() : "";

                if (email.isEmpty()) continue;

                var dto = new EmailRecipientDto(name, email);
                recipients.add(dto);

            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }

        return recipients;

    }
}
