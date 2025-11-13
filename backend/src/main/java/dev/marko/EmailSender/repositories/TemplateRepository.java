package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.EmailTemplate;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository
        extends JpaRepository<EmailTemplate, Long>, UserScopedRepository<EmailTemplate> {

    List<EmailTemplate> findAllByUserId(Long userId);

    Optional<EmailTemplate> findByIdAndUserId(Long id, Long userId);

}
