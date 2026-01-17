package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, Long> {
    
    @Query("SELECT e FROM EmailMessage e LEFT JOIN FETCH e.user LEFT JOIN FETCH e.campaign LEFT JOIN FETCH e.emailTemplate LEFT JOIN FETCH e.smtpCredentials WHERE e.campaign = :campaign")
    List<EmailMessage> findAllByCampaign(@Param("campaign") Campaign campaign);
    
    @Query("SELECT e FROM EmailMessage e LEFT JOIN FETCH e.user LEFT JOIN FETCH e.campaign LEFT JOIN FETCH e.emailTemplate LEFT JOIN FETCH e.smtpCredentials WHERE e.campaign.id = :campaignId AND e.status = :status")
    List<EmailMessage> findAllByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") Status status);
    
    @Query("SELECT e FROM EmailMessage e LEFT JOIN FETCH e.user LEFT JOIN FETCH e.campaign LEFT JOIN FETCH e.emailTemplate LEFT JOIN FETCH e.smtpCredentials WHERE e.user.id = :userId AND e.status IN :statuses")
    List<EmailMessage> findAllByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<Status> status);
    
    @Query("SELECT e FROM EmailMessage e LEFT JOIN FETCH e.user LEFT JOIN FETCH e.campaign LEFT JOIN FETCH e.emailTemplate LEFT JOIN FETCH e.smtpCredentials WHERE e.campaign.id = :campaignId AND e.user.id = :userId AND e.status IN :statuses")
    List<EmailMessage> findAllByCampaignIdAndUserIdAndStatusIn(@Param("campaignId") Long campaignId, @Param("userId") Long userId, @Param("statuses") List<Status> status);


    @Query("SELECT m FROM EmailMessage m LEFT JOIN FETCH m.user LEFT JOIN FETCH m.campaign LEFT JOIN FETCH m.emailTemplate LEFT JOIN FETCH m.smtpCredentials WHERE m.messageId = :messageId AND m.status = 'SENT'")
    Optional<EmailMessage> findByMessageId(@Param("messageId") String messageId);

    @Query("SELECT e FROM EmailMessage e LEFT JOIN FETCH e.user LEFT JOIN FETCH e.campaign LEFT JOIN FETCH e.emailTemplate LEFT JOIN FETCH e.smtpCredentials WHERE e.id = :id AND e.user.id = :userId")
    Optional<EmailMessage> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT m FROM EmailMessage m LEFT JOIN FETCH m.user LEFT JOIN FETCH m.campaign LEFT JOIN FETCH m.emailTemplate LEFT JOIN FETCH m.smtpCredentials WHERE m.messageId = :messageId AND m.status = :status")
    Optional<EmailMessage> findByMessageIdAndStatus(@Param("messageId") String messageId, @Param("status") Status status);
    boolean existsByInReplyToAndFollowUpTemplate(String inReplyTo, FollowUpTemplate followUpTemplate);
    boolean existsByInReplyToAndFollowUpTemplateId(String inReplyTo, Long followUpTemplateId);


    @Query("""
    SELECT DISTINCT e FROM EmailMessage e
    JOIN FETCH e.campaign c
    LEFT JOIN FETCH c.followUpTemplates
    LEFT JOIN FETCH e.user
    LEFT JOIN FETCH e.emailTemplate
    LEFT JOIN FETCH e.smtpCredentials
    WHERE e.status = 'SENT'
    AND NOT EXISTS (
        SELECT r FROM EmailReply r WHERE r.emailMessage = e
    )
""")
    List<EmailMessage> findSentWithoutReply();

    @Query("""
    SELECT DISTINCT e FROM EmailMessage e
    JOIN FETCH e.campaign c
    LEFT JOIN FETCH c.followUpTemplates
    LEFT JOIN FETCH e.user
    LEFT JOIN FETCH e.emailTemplate
    LEFT JOIN FETCH e.smtpCredentials
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