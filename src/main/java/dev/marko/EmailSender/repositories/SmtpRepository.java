package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.SmtpCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmtpRepository extends JpaRepository<SmtpCredentials,Long> {

    List<SmtpCredentials> findAllByUserId(Long userId);

    Optional<SmtpCredentials> findByIdAndUserId(Long id, Long userId);

    Optional<SmtpCredentials> findByEmailAndUserId(String email, Long userId);

}
