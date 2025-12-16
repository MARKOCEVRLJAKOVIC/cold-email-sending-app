package dev.marko.EmailSender.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "email_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_name")
    private String recipientName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @PrePersist
    @PreUpdate
    private void prePersistOrUpdate() {
        if (this.status == Status.SENT && this.sentAt == null) {
            this.sentAt = LocalDateTime.now(ZoneId.of("UTC"));
        }
    }

    @Lob
    @Column(name = "sent_message", columnDefinition = "TEXT")
    private String sentMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING;


    @Column(name = "message_id", unique = true)
    private String messageId;

    @Column(name = "in_reply_to")
    private String inReplyTo;

    @Lob
    @Column(
            name = "error_message",
            columnDefinition = "LONGTEXT"
    )    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id")
    private EmailTemplate emailTemplate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "smtp_id")
    private SmtpCredentials smtpCredentials;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_up_template_id")
    private FollowUpTemplate followUpTemplate;

    @OneToMany(mappedBy = "emailMessage")
    private List<EmailReply> emailReplies = new ArrayList<>();


}
