package dev.marko.EmailSender.services.base;

import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.Getter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

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

    @Transactional(readOnly = true)
    public List<D> getAll() {
        var user = currentUserProvider.getCurrentUser();
        return toListDto(repository.findAllByUserId(user.getId()));
    }

    public D getById(Long id) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(notFound);
        return toDto(entity);
    }

    @Transactional
    public D create(C request) {
        var user = currentUserProvider.getCurrentUser();
        var entity = toEntity(request);
        setUser(entity, user);
        repository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public D update(Long id, U request) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(notFound);

        updateEntity(entity, request);
        repository.save(entity);

        return toDto(entity);
    }

    @Transactional
    public void delete(Long id) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(notFound);

        repository.delete(entity);
    }
}