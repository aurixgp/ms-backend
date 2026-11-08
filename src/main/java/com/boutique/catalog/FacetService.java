package com.boutique.catalog;

import com.boutique.catalog.CatalogDtos.FacetValue;
import com.boutique.catalog.CatalogDtos.Facets;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class FacetService {

    private final EntityManager em;
    private final CategoryService categories;

    public FacetService(EntityManager em, CategoryService categories) {
        this.em = em;
        this.categories = categories;
    }

    public Facets facets(ProductFilter f) {
        Set<Long> catIds = categories.resolve(f.category(), f.gender());
        return new Facets(
                group(f.withoutBrands(), catIds, "brand", false),
                group(f.withoutColors(), catIds, "color", true),
                group(f.withoutMaterials(), catIds, "material", false),
                group(f.withoutPatterns(), catIds, "pattern", false),
                priceBound(f.withoutPrice(), catIds, true),
                priceBound(f.withoutPrice(), catIds, false),
                count(f, catIds));
    }

    private List<FacetValue> group(ProductFilter f, Set<Long> catIds, String attribute, boolean withHex) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Product> root = cq.from(Product.class);
        Join<Object, Object> j = root.join(attribute);
        Expression<Long> count = cb.count(root);

        if (withHex) {
            cq.multiselect(j.get("slug"), j.get("name"), count, j.get("hex"))
              .groupBy(j.get("slug"), j.get("name"), j.get("hex"));
        } else {
            cq.multiselect(j.get("slug"), j.get("name"), count)
              .groupBy(j.get("slug"), j.get("name"));
        }
        cq.where(ProductSpecifications.storefront(f, catIds).toPredicate(root, cq, cb))
          .orderBy(withHex ? cb.desc(count) : cb.asc(j.get("name")));

        return em.createQuery(cq).getResultList().stream()
                .map(t -> new FacetValue(
                        t.get(0, String.class), t.get(1, String.class), t.get(2, Long.class),
                        withHex ? t.get(3, String.class) : null))
                .toList();
    }

    private BigDecimal priceBound(ProductFilter f, Set<Long> catIds, boolean min) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Product> root = cq.from(Product.class);
        Path<BigDecimal> price = root.get("price");
        cq.select(min ? cb.min(price) : cb.max(price))
          .where(ProductSpecifications.storefront(f, catIds).toPredicate(root, cq, cb));
        return em.createQuery(cq).getSingleResult();
    }

    private long count(ProductFilter f, Set<Long> catIds) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Product> root = cq.from(Product.class);
        cq.select(cb.count(root)).where(ProductSpecifications.storefront(f, catIds).toPredicate(root, cq, cb));
        return em.createQuery(cq).getSingleResult();
    }
}
