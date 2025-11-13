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


    public List<D> getAll() {
        var user = currentUserProvider.getCurrentUser();
        var entities = repository.findAll(); // možeš kasnije filtrirati po userId
        return entities.stream().map(this::toDto).toList();
    }

    public D getById(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        return toDto(entity);
    }

    public D create(C request) {
        var entity = toEntity(request);
        repository.save(entity);
        return toDto(entity);
    }

    public D update(Long id, C request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        updateEntity(entity, request);
        repository.save(entity);
        return toDto(entity);
    }

    public void delete(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        repository.delete(entity);
    }
}
