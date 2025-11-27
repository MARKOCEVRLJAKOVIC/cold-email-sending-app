package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, Long> {
    List<EmailMessage> findAllByCampaign(Campaign campaign);
    List<EmailMessage> findAllByCampaignIdAndStatus(Long campaignId, Status status);
    List<EmailMessage> findAllByUserIdAndStatusIn(Long userId, List<Status> status);
    List<EmailMessage> findAllByCampaignIdAndUserIdAndStatusIn(Long campaignId, Long userId, List<Status> status);


    @Query("SELECT m FROM EmailMessage m WHERE m.messageId = :messageId AND m.status = 'SENT'")
    Optional<EmailMessage> findByMessageId(String messageId);

    Optional<EmailMessage> findByIdAndUserId(Long id, Long userId);

    Optional<EmailMessage> findByMessageIdAndStatus(String messageId, Status status);
    boolean existsByInReplyToAndFollowUpTemplate(String inReplyTo, FollowUpTemplate followUpTemplate);
    boolean existsByInReplyToAndFollowUpTemplateId(String inReplyTo, Long followUpTemplateId);


    @Query("""
    SELECT e FROM EmailMessage e
    JOIN FETCH e.campaign c
    LEFT JOIN FETCH c.followUpTemplates
    WHERE e.status = 'SENT'
    AND NOT EXISTS (
        SELECT r FROM EmailReply r WHERE r.emailMessage = e
    )
""")
    List<EmailMessage> findSentWithoutReply();

    @Query("""
    SELECT e FROM EmailMessage e
    JOIN FETCH e.campaign c
    LEFT JOIN FETCH c.followUpTemplates
    WHERE e.status = 'SENT'
    AND NOT EXISTS (
        SELECT r FROM EmailReply r WHERE r.emailMessage = e
    )
    AND NOT EXISTS (
    SELECT f FROM EmailMessage f WHERE f.inReplyTo = e.messageId
    )
    """)
    List<EmailMessage> findSentWithoutReplyOrFollowUp();



}