package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, Long> {

    Optional<EmailMessage> findTopByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(Status status, LocalDateTime time);

    @Query("""
    SELECT e FROM EmailMessage e
    WHERE e.user.id = :userId
      AND e.status = :status
      AND e.scheduledAt <= :now
    ORDER BY e.scheduledAt ASC""")
    List<EmailMessage> findTopByUserIdAndStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
            @Param("userId") Long userId,
            @Param("status") Status status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    List<EmailMessage> findAllByCampaign(Campaign campaign);

}