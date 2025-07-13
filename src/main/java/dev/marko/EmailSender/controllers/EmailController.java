package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.UserNotFoundException;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
@RestController
@RequestMapping("/emails")
public class EmailController {

    private final EmailMessageRepository emailMessageRepository;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsvWithTemplates(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("subjects") List<String> subjects,
            @RequestParam("templates") List<String> templates
    ) {
        if (file.isEmpty() || subjects.isEmpty() || templates.isEmpty()) {
            return ResponseEntity.badRequest().body("File subject or template is missing");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

            List<EmailMessage> emails = new ArrayList<>();
            String line;
            boolean first = true;

            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue; // skip header
                }

                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;

                String recipientName = tokens[0].trim();
                String recipientEmail = tokens[1].trim();

                String randomSubject = subjects.get(new Random().nextInt(subjects.size()));
                String randomTemplate = templates.get(new Random().nextInt(templates.size()));

                // personalize message with {name}
                String personalizedMessage = randomTemplate.replace("{name}", recipientName);

                emails.add(EmailMessage.builder()
                        .recipientName(recipientName)
                        .recipientEmail(recipientEmail)
                        .subject(randomSubject)
                        .chosenTemplate(randomTemplate)
                        .message(personalizedMessage)
                        .status("PENDING")
                        .user(user)
                        .build());
            }

            emailMessageRepository.saveAll(emails);

            return ResponseEntity.ok("Loaded and saved " + emails.size() + " mails.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error trying to process your csv file");
        }
    }
}
