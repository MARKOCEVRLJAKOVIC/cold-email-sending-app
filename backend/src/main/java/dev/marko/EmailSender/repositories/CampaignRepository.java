package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, UserScopedRepository<Campaign> {

    List<Campaign> findAllByUserId(Long userId);

    Optional<Campaign> findByIdAndUserId(Long id, Long userId);

}
