package com.boutique.admin;

import com.boutique.catalog.CatalogRepository;
import com.boutique.catalog.NamedEntity;
import com.boutique.common.Exceptions;
import com.boutique.common.RaQuery;
import com.boutique.common.RaResponse;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CRUD au format ra-data-simple-rest pour les référentiels simples
 * (marques, couleurs, matières, motifs, catégories).
 */
public abstract class AdminCrudController<E extends NamedEntity, D> {

    private final CatalogRepository<E> repo;
    private final String resource;

    protected AdminCrudController(CatalogRepository<E> repo, String resource) {
        this.repo = repo;
        this.resource = resource;
    }

    protected abstract D toDto(E entity);

    protected abstract E newEntity();

    protected abstract void apply(D dto, E entity);

    protected Map<String, String> sortableFields() {
        return Map.of("id", "id", "name", "name", "slug", "slug");
    }

    /** Filtres propres à une ressource, en plus de "q" et "id". */
    protected void addFilters(RaQuery q, List<Predicate> predicates,
                              jakarta.persistence.criteria.Root<E> root,
                              jakarta.persistence.criteria.CriteriaBuilder cb) {}

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<D>> list(@RequestParam(required = false) String sort,
                                        @RequestParam(required = false) String range,
                                        @RequestParam(required = false) String filter) {
        RaQuery q = RaQuery.parse(sort, range, filter);
        List<Long> ids = q.ids();
        Specification<E> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (ids != null) p.add(root.get("id").in(ids));
            String text = q.text("q");
            if (text != null) {
                String like = "%" + text.toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("slug")), like)));
            }
            addFilters(q, p, root, cb);
            return cb.and(p.toArray(Predicate[]::new));
        };
        Pageable pageable = ids != null ? PageRequest.of(0, Math.max(ids.size(), 1)) : q.pageable(sortableFields());
        return RaResponse.list(resource, repo.findAll(spec, pageable), this::toDto);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public D one(@PathVariable Long id) {
        return toDto(find(id));
    }

    @PostMapping
    @Transactional
    public D create(@Valid @RequestBody D dto) {
        E entity = newEntity();
        apply(dto, entity);
        return toDto(repo.saveAndFlush(entity));
    }

    @PutMapping("/{id}")
    @Transactional
    public D update(@PathVariable Long id, @Valid @RequestBody D dto) {
        E entity = find(id);
        apply(dto, entity);
        return toDto(repo.saveAndFlush(entity));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public D delete(@PathVariable Long id) {
        E entity = find(id);
        D dto = toDto(entity);
        repo.delete(entity);
        repo.flush();
        return dto;
    }

    protected E find(Long id) {
        return repo.findById(id).orElseThrow(() -> new Exceptions.NotFound(resource + " " + id + " introuvable"));
    }
}
