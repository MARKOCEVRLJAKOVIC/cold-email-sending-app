package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailMessageRepository extends JpaRepository<EmailMessage,Long> {

    List<EmailMessage> findAllByUser(User user);
    List<EmailMessage> findAllByUserAndStatus(User user, String status);

}
