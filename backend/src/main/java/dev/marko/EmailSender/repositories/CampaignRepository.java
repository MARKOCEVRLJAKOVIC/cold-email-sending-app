package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, UserScopedRepository<Campaign> {

    @Query("SELECT c FROM Campaign c LEFT JOIN FETCH c.user WHERE c.user.id = :userId")
    List<Campaign> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Campaign c LEFT JOIN FETCH c.user WHERE c.id = :id AND c.user.id = :userId")
    Optional<Campaign> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT c FROM Campaign c LEFT JOIN FETCH c.user")
    List<Campaign> findAllWithUser();

    @Query("SELECT c FROM Campaign c LEFT JOIN FETCH c.user WHERE c.id = :id")
    Optional<Campaign> findByIdWithUser(@Param("id") Long id);

}
