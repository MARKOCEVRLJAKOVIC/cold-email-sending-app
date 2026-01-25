package dev.marko.EmailSender.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "enabled")
    private Boolean enabled;

    @OneToMany(mappedBy = "user")
    private List<SmtpCredentials> credentials = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<EmailTemplate> templates = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Campaign> campaigns = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<FollowUpTemplate> followUpTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<EmailReply> emailReplies = new ArrayList<>();
}