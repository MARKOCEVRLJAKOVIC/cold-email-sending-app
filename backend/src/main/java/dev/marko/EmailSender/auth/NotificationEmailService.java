package dev.marko.EmailSender.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class NotificationEmailService {

    private final JavaMailSender mailSender;

    String confirmationMessage = "Dear user, Thank you for registering! To complete your sign-up, please kindly confirm your email address by clicking the link below: ";

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public String confirmationMessage(){
        return confirmationMessage;
    }

    public String buildEmailFromTemplate(String templateName, Map<String, String> variables) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + templateName);
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return content;
        } catch (Exception e) {
            throw new RuntimeException("Could not load email template", e);
        }
    }

}
