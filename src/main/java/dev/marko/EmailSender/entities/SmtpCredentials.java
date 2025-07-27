package dev.marko.EmailSender.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "smtp_credentials")
public class SmtpCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sender_email")
    private String email;

    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_username")
    private String smtpUsername;

    @Column(name = "smtp_password")
    private String smtpPassword;

    @Column(name = "oauth_access_token", columnDefinition = "TEXT")
    private String oauthAccessToken;

    @Column(name = "oauth_refresh_token", columnDefinition = "TEXT")
    private String oauthRefreshToken;

    @Column(name = "token_expires_at")
    private Long tokenExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "smtp_type")
    private SmtpType smtpType;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "smtpCredentials")
    private List<EmailMessage> emailMessages = new ArrayList<>();

    public boolean isGmailOauth() {
        return smtpType == SmtpType.OAUTH2 && smtpHost.contains("gmail");
    }

}
