package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "verification_token")
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public LocalDateTime expirationDate;


    public VerificationToken(String token, User user, LocalDateTime localDateTime) {
    }
}
