package com.boutique.admin;

import com.boutique.catalog.*;
import com.boutique.common.Exceptions;
import com.boutique.common.RaQuery;
import com.boutique.common.Slugs;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Référentiels du catalogue gérés depuis le back-office. */
public final class ReferenceAdminControllers {

    private ReferenceAdminControllers() {}

    public record BrandDto(Long id, @NotBlank String name, String slug, boolean premium, String logoUrl) {}

    public record ColorDto(Long id, @NotBlank String name, String slug,
                           @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "format #RRGGBB attendu") String hex) {}

    public record SimpleDto(Long id, @NotBlank String name, String slug) {}

    public record CategoryDto(Long id, @NotBlank String name, String slug, Long parentId, int position, String universe) {}

    @RestController
    @RequestMapping("/api/admin/brands")
    public static class Brands extends AdminCrudController<Brand, BrandDto> {
        public Brands(BrandRepository repo) { super(repo, "brands"); }

        @Override protected BrandDto toDto(Brand b) { return new BrandDto(b.getId(), b.getName(), b.getSlug(), b.isPremium(), b.getLogoUrl()); }
        @Override protected Brand newEntity() { return new Brand(); }
        @Override protected void apply(BrandDto d, Brand b) {
            b.setName(d.name().trim());
            b.setSlug(Slugs.orGenerate(d.slug(), d.name()));
            b.setPremium(d.premium());
            b.setLogoUrl(d.logoUrl() == null || d.logoUrl().isBlank() ? null : d.logoUrl());
        }
        @Override protected Map<String, String> sortableFields() {
            return Map.of("id", "id", "name", "name", "slug", "slug", "premium", "premium");
        }
    }

    @RestController
    @RequestMapping("/api/admin/colors")
    public static class Colors extends AdminCrudController<Color, ColorDto> {
        public Colors(ColorRepository repo) { super(repo, "colors"); }

        @Override protected ColorDto toDto(Color c) { return new ColorDto(c.getId(), c.getName(), c.getSlug(), c.getHex()); }
        @Override protected Color newEntity() { return new Color(); }
        @Override protected void apply(ColorDto d, Color c) {
            c.setName(d.name().trim());
            c.setSlug(Slugs.orGenerate(d.slug(), d.name()));
            c.setHex(d.hex().toLowerCase());
        }
    }

    @RestController
    @RequestMapping("/api/admin/materials")
    public static class Materials extends AdminCrudController<Material, SimpleDto> {
        public Materials(MaterialRepository repo) { super(repo, "materials"); }

        @Override protected SimpleDto toDto(Material m) { return new SimpleDto(m.getId(), m.getName(), m.getSlug()); }
        @Override protected Material newEntity() { return new Material(); }
        @Override protected void apply(SimpleDto d, Material m) {
            m.setName(d.name().trim());
            m.setSlug(Slugs.orGenerate(d.slug(), d.name()));
        }
    }

    @RestController
    @RequestMapping("/api/admin/patterns")
    public static class Patterns extends AdminCrudController<com.boutique.catalog.Pattern, SimpleDto> {
        public Patterns(PatternRepository repo) { super(repo, "patterns"); }

        @Override protected SimpleDto toDto(com.boutique.catalog.Pattern p) { return new SimpleDto(p.getId(), p.getName(), p.getSlug()); }
        @Override protected com.boutique.catalog.Pattern newEntity() { return new com.boutique.catalog.Pattern(); }
        @Override protected void apply(SimpleDto d, com.boutique.catalog.Pattern p) {
            p.setName(d.name().trim());
            p.setSlug(Slugs.orGenerate(d.slug(), d.name()));
        }
    }

    @RestController
    @RequestMapping("/api/admin/categories")
    public static class Categories extends AdminCrudController<Category, CategoryDto> {
        private final CategoryRepository categories;

        public Categories(CategoryRepository repo) {
            super(repo, "categories");
            this.categories = repo;
        }

        @Override protected CategoryDto toDto(Category c) {
            return new CategoryDto(c.getId(), c.getName(), c.getSlug(),
                    c.getParent() == null ? null : c.getParent().getId(), c.getPosition(), c.getUniverse());
        }
        @Override protected Category newEntity() { return new Category(); }
        @Override protected void apply(CategoryDto d, Category c) {
            if (d.parentId() != null && d.parentId().equals(c.getId())) {
                throw new Exceptions.BadRequest("Une catégorie ne peut pas être son propre parent");
            }
            c.setName(d.name().trim());
            c.setSlug(Slugs.orGenerate(d.slug(), d.name()));
            c.setPosition(d.position());
            Category parent = d.parentId() == null ? null : categories.findById(d.parentId())
                    .orElseThrow(() -> new Exceptions.BadRequest("Catégorie parente introuvable"));
            c.setParent(parent);
            // Une sous-rubrique hérite de l'univers de son parent si rien n'est précisé
            String universe = d.universe() == null || d.universe().isBlank() ? null : d.universe().trim().toLowerCase();
            c.setUniverse(universe == null && parent != null ? parent.getUniverse() : universe);
        }
        @Override protected Map<String, String> sortableFields() {
            return Map.of("id", "id", "name", "name", "slug", "slug", "position", "position", "parentId", "parent.id");
        }
        @Override protected void addFilters(RaQuery q, List<Predicate> p, Root<Category> root, CriteriaBuilder cb) {
            Long parentId = q.longValue("parentId");
            if (parentId != null) p.add(cb.equal(root.get("parent").get("id"), parentId));
            String universe = q.text("universe");
            if (universe != null) p.add(cb.equal(root.get("universe"), universe));
        }
    }
}
