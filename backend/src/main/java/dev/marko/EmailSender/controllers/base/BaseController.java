package dev.marko.EmailSender.controllers.base;

import dev.marko.EmailSender.services.base.BaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<D, C, U> {

    protected final BaseService<?, D, C, ?, U> service;

    protected BaseController(BaseService<?, D, C, ?, U> service) {
        this.service = service;
    }

    @GetMapping
    public List<D> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public D getById(@PathVariable @Min(1) Long id) {
        return service.getById(id);
    }

    @PostMapping
    public D create(@RequestBody @Valid C request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public D update(@PathVariable @Valid Long id, @RequestBody U request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}