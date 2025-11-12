package dev.marko.EmailSender.services.base;

import dev.marko.EmailSender.security.CurrentUserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class BaseService<E, D, C, R extends JpaRepository<E, Long>> {

    public final R repository;
    public final CurrentUserProvider currentUserProvider;

    protected BaseService(R repository, CurrentUserProvider currentUserProvider) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
    }


    protected abstract D toDto(E entity);
    protected abstract E toEntity(C createRequest);
    protected abstract void updateEntity(E entity, C request);





}
