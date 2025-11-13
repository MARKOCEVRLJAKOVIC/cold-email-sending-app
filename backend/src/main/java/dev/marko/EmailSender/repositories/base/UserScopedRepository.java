package dev.marko.EmailSender.repositories.base;

import java.util.List;
import java.util.Optional;

public interface UserScopedRepository<E> {
    List<E> findAllByUserId(Long userId);
    Optional<E> findByIdAndUserId(Long id, Long userId);
}
