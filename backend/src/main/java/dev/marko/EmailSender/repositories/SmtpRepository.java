package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmtpRepository extends JpaRepository<SmtpCredentials,Long>, UserScopedRepository<SmtpCredentials> {

    List<SmtpCredentials> findAllByUserId(Long userId);

    Optional<SmtpCredentials> findByIdAndUserId(Long id, Long userId);

    Optional<SmtpCredentials> findByEmailAndUserId(String email, Long userId);

    Optional<SmtpCredentials> findByEmail(String email);

    List<SmtpCredentials> findAllBySmtpTypeAndUserId(SmtpType smtpType, Long id);

    List<SmtpCredentials> findAllBySmtpTypeAndEnabled(SmtpType smtpType, Boolean enabled);




}
