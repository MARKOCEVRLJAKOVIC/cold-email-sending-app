package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.EmailTemplate;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository
        extends JpaRepository<EmailTemplate, Long>, UserScopedRepository<EmailTemplate> {
    
    @Query("SELECT t FROM EmailTemplate t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.campaign WHERE t.user.id = :userId")
    List<EmailTemplate> findAllByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM EmailTemplate t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.campaign WHERE t.id = :id AND t.user.id = :userId")
    Optional<EmailTemplate> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
