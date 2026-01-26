package dev.marko.EmailSender.services.base;

import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.Getter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

/**
 * Base service class providing common CRUD operations for user-scoped entities.
 * All operations are automatically scoped to the current user.
 *
 * @param <E> the entity type
 * @param <D> the DTO type
 * @param <C> the create request type
 * @param <R> the repository type
 * @param <U> the update request type
 */
@Getter
public abstract class BaseService<
        E,
        D,
        C,
        R extends UserScopedRepository<E> & JpaRepository<E, Long>,
        U
        > {

    protected final R repository;
    protected final CurrentUserProvider currentUserProvider;
    private final Supplier<RuntimeException> notFound;

    protected BaseService(
            R repository,
            CurrentUserProvider currentUserProvider,
            Supplier<RuntimeException> notFound
    ) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
        this.notFound = notFound;
    }

    protected abstract D toDto(E entity);
    protected abstract E toEntity(C request);
    protected abstract void updateEntity(E entity, U request);

    /** every entity must have a user */
    protected abstract void setUser(E entity, User user);

    protected List<D> toListDto(List<E> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retrieves all entities belonging to the current user.
     *
     * @return list of DTOs for all user's entities
     */
    @Transactional(readOnly = true)
    public List<D> getAll() {
        var user = currentUserProvider.getCurrentUser();
        return toListDto(repository.findAllByUserId(user.getId()));
    }

    /**
     * Retrieves an entity by ID, ensuring it belongs to the current user.
     *
     * @param id the entity ID
     * @return the DTO for the entity
     * @throws RuntimeException if the entity is not found or doesn't belong to the user
     */
    public D getById(Long id) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(notFound);
        return toDto(entity);
    }

    /**
     * Creates a new entity from the request and associates it with the current user.
     *
     * @param request the create request
     * @return the DTO for the created entity
     */
    @Transactional
    public D create(C request) {
        var user = currentUserProvider.getCurrentUser();
        var entity = toEntity(request);
        setUser(entity, user);
        repository.save(entity);
        return toDto(entity);
    }

    /**
     * Updates an existing entity, ensuring it belongs to the current user.
     *
     * @param id the entity ID
     * @param request the update request
     * @return the DTO for the updated entity
     * @throws RuntimeException if the entity is not found or doesn't belong to the user
     */
    @Transactional
    public D update(Long id, U request) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(notFound);

        updateEntity(entity, request);
        repository.save(entity);

        return toDto(entity);
    }

    /**
     * Deletes an entity by ID, ensuring it belongs to the current user.
     *
     * @param id the entity ID
     * @throws RuntimeException if the entity is not found or doesn't belong to the user
     */
    @Transactional
    public void delete(Long id) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(notFound);

        repository.delete(entity);
    }
}