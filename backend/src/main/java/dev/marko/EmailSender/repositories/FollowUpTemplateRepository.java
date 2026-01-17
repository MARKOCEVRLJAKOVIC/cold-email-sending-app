package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.FollowUpTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowUpTemplateRepository extends JpaRepository<FollowUpTemplate, Long> {

    @Query("SELECT f FROM FollowUpTemplate f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.campaign WHERE f.user.id = :userId")
    List<FollowUpTemplate> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM FollowUpTemplate f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.campaign WHERE f.id = :id AND f.user.id = :userId")
    Optional<FollowUpTemplate> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

}
