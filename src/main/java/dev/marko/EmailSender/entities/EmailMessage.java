package dev.marko.EmailSender.entities;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "subject")
    private String subject;

    @Lob
    @Column(name = "message")
    private String message;

    @Lob
    @Column(name = "chosen_template")
    private String chosenTemplate;

    @Column(name = "status")
    private String status;  // npr. PENDING, SENT, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
