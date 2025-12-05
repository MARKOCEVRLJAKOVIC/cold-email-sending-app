package dev.marko.EmailSender.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "timezone")
    private String timezone = "Europe/Belgrade";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailTemplate> emailTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL)
    private List<EmailMessage> emailMessages = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL)
    private List<FollowUpTemplate> followUpTemplates = new ArrayList<>();

}