package dev.marko.EmailSender.controllers.base;

import dev.marko.EmailSender.services.base.BaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Base REST controller providing standard CRUD endpoints.
 * All operations delegate to the underlying service and are automatically scoped to the current user.
 *
 * @param <D> the DTO type
 * @param <C> the create request type
 * @param <U> the update request type
 */
public abstract class BaseController<D, C, U> {

    protected final BaseService<?, D, C, ?, U> service;

    protected BaseController(BaseService<?, D, C, ?, U> service) {
        this.service = service;
    }

    /**
     * Retrieves all entities for the current user.
     *
     * @return list of all user's entities
     */
    @GetMapping
    public List<D> getAll() {
        return service.getAll();
    }

    /**
     * Retrieves an entity by ID for the current user.
     *
     * @param id the entity ID (must be at least 1)
     * @return the entity DTO
     */
    @GetMapping("/{id}")
    public D getById(@PathVariable @Min(1) Long id) {
        return service.getById(id);
    }

    /**
     * Creates a new entity for the current user.
     *
     * @param request the create request
     * @return the created entity DTO
     */
    @PostMapping
    public D create(@RequestBody @Valid C request) {
        return service.create(request);
    }

    /**
     * Updates an existing entity for the current user.
     *
     * @param id the entity ID
     * @param request the update request
     * @return the updated entity DTO
     */
    @PutMapping("/{id}")
    public D update(@PathVariable @Valid Long id, @RequestBody U request) {
        return service.update(id, request);
    }

    /**
     * Deletes an entity by ID for the current user.
     *
     * @param id the entity ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}