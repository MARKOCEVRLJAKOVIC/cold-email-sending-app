package dev.marko.EmailSender.repositories;

import dev.marko.EmailSender.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.credentials WHERE u.id = :id")
    Optional<User> findByIdWithCredentials(@Param("id") Long id);

}

