package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign,Long> {

    List<Campaign> findAllByUserId(Long userId);

    Optional<Campaign> findByIdAndUserId(Long id, Long userId);
}
