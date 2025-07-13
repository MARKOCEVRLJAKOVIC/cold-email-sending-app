package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.Smtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmtpRepository extends JpaRepository<Smtp,Long> {
    List<Smtp> findAllByUserId(Long userId);
}
