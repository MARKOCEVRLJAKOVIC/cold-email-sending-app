package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.FollowUpTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowUpTemplateRepository extends JpaRepository<FollowUpTemplate, Long> {

    List<FollowUpTemplate> findAllByUserId(Long userId);

    Optional<FollowUpTemplate> findByIdAndUserId(Long id, Long userId);

}
