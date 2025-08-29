package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.EmailReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailReplyRepository extends JpaRepository<EmailReply, Long> {

    Optional<EmailReply> findByIdAndUserId(Long id, Long userId);
    List<EmailReply> findAllByUserId(Long userId);

}
