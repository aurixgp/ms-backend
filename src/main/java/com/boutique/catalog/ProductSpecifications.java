package com.boutique.catalog;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    /** Requête du catalogue public : uniquement les produits actifs. */
    public static Specification<Product> storefront(ProductFilter f, Collection<Long> categoryIds) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isTrue(root.get("active")));

            if (categoryIds != null && !categoryIds.isEmpty()) {
                p.add(root.get("category").get("id").in(categoryIds));
            }
            if (f.universe() != null && !f.universe().isBlank()) {
                p.add(cb.equal(root.get("category").get("universe"), f.universe()));
            }
            if (notEmpty(f.brands()))    p.add(root.join("brand").get("slug").in(f.brands()));
            if (notEmpty(f.colors()))    p.add(root.join("color").get("slug").in(f.colors()));
            if (notEmpty(f.materials())) p.add(root.join("material").get("slug").in(f.materials()));
            if (notEmpty(f.patterns()))  p.add(root.join("pattern").get("slug").in(f.patterns()));
            if (f.minPrice() != null)    p.add(cb.greaterThanOrEqualTo(root.get("price"), f.minPrice()));
            if (f.maxPrice() != null)    p.add(cb.lessThanOrEqualTo(root.get("price"), f.maxPrice()));
            if (f.minRating() != null)   p.add(cb.greaterThanOrEqualTo(root.get("rating"), f.minRating()));
            if (Boolean.TRUE.equals(f.onSale()))  p.add(cb.isNotNull(root.get("originalPrice")));
            if (Boolean.TRUE.equals(f.newOnly())) p.add(cb.isTrue(root.get("newArrival")));
            if (Boolean.TRUE.equals(f.premium())) p.add(cb.isTrue(root.join("brand").get("premium")));
            if (f.q() != null && !f.q().isBlank()) {
                String like = "%" + f.q().trim().toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.join("brand").get("name")), like)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    private static boolean notEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }
}
