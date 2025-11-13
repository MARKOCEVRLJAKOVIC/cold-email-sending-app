package dev.marko.EmailSender.services.base;

import dev.marko.EmailSender.repositories.base.UserScopedRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.Getter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Getter
public abstract class BaseService<E, D, C,
        R extends JpaRepository<E, Long> & UserScopedRepository<E>> {

    public final R repository;
    public final CurrentUserProvider currentUserProvider;

    protected BaseService(R repository, CurrentUserProvider currentUserProvider) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
    }

    protected abstract RuntimeException notFoundException();



    protected abstract D toDto(E entity);
    protected abstract E toEntity(C createRequest);
    protected abstract void updateEntity(E entity, C request);


    public List<D> getAll() {
        var user = currentUserProvider.getCurrentUser();
        var entities = repository.findAllByUserId(user.getId());
        return entities.stream().map(this::toDto).toList();
    }

    public D getById(Long id) {
        var user = currentUserProvider.getCurrentUser();

        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(this::notFoundException);
        return toDto(entity);
    }

    public D create(C request) {
        var entity = toEntity(request);
        repository.save(entity);
        return toDto(entity);
    }

    public D update(Long id, C request) {

        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(this::notFoundException);
        updateEntity(entity, request);
        repository.save(entity);
        return toDto(entity);
    }

    public void delete(Long id) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(this::notFoundException);
        repository.delete(entity);
    }

}
