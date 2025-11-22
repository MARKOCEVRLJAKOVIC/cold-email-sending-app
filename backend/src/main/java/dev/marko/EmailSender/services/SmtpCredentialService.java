package dev.marko.EmailSender.services;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class SmtpCredentialService {

    private final SmtpRepository smtpRepository;
    private final CurrentUserProvider currentUserProvider;

    public Optional<SmtpCredentials> findByEmailAndUser(String email) {
        var user = currentUserProvider.getCurrentUser();
        return smtpRepository.findByEmailAndUserId(email, user.getId());
    }

    public SmtpCredentials save(SmtpCredentials smtpCredentials) {
        return smtpRepository.save(smtpCredentials);
    }

}
