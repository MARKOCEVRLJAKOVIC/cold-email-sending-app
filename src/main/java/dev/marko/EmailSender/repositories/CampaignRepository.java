package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign,Long> {

    List<Campaign> findAllByUserId(Long userId);
}
