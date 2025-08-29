package dev.marko.EmailSender.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "follow_up_templates")
public class FollowUpTemplate {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delay_days")
    private Integer delayDays;

    @Lob
    @Column(name = "message")
    private String message;

    @Column(name = "template_order")
    private Integer templateOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "followUpTemplate")
    private List<EmailMessage> emailMessages = new ArrayList<>();
}
