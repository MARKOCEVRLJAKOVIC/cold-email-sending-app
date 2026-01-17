package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SmtpRepository extends JpaRepository<SmtpCredentials,Long>, UserScopedRepository<SmtpCredentials> {

    @Query("SELECT s FROM SmtpCredentials s LEFT JOIN FETCH s.user WHERE s.user.id = :userId")
    List<SmtpCredentials> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM SmtpCredentials s LEFT JOIN FETCH s.user WHERE s.id = :id AND s.user.id = :userId")
    Optional<SmtpCredentials> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    Optional<SmtpCredentials> findByEmailAndUserId(String email, Long userId);

    Optional<SmtpCredentials> findByEmail(String email);

    List<SmtpCredentials> findAllBySmtpTypeAndUserId(SmtpType smtpType, Long id);

    List<SmtpCredentials> findAllBySmtpTypeAndEnabled(SmtpType smtpType, Boolean enabled);




}
