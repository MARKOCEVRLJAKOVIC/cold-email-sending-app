package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.exception.InvalidCsvException;
import dev.marko.EmailSender.exception.InvalidEmailFormatException;
import dev.marko.EmailSender.exception.InvalidPrincipalException;
import lombok.AllArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
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

    private static final EmailValidator emailValidator = EmailValidator.getInstance();

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
                name = name.split(" ")[0];

                if (email.isEmpty()) continue;
//                if(!emailValidator.isValid(email)) throw new InvalidEmailFormatException("Invalid email format: " + email);

                var dto = new EmailRecipientDto(name, email);
                recipients.add(dto);

            }

        } catch (IOException e) {
            throw new InvalidCsvException("Failed to read CSV file", e);
        }

        return recipients;

    }
}
