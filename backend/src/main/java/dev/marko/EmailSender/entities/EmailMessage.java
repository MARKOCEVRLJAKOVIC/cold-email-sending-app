package dev.marko.EmailSender.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TimeZoneColumn;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
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

    @PrePersist
    @PreUpdate
    private void prePersistOrUpdate() {
        if (this.status == Status.SENT && this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    @Lob
    @Column(name = "sent_message")
    private String sentMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "scheduled_at")
    LocalDateTime scheduledAt;

    @Column(name = "message_id", unique = true)
    private String messageId;

    @Column(name = "in_reply_to")
    private String inReplyTo;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

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
