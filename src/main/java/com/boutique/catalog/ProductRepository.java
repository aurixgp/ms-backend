package com.boutique.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends CatalogRepository<Product> {

    @Override
    @EntityGraph(attributePaths = {"brand", "color", "category"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "color", "category", "material", "pattern"})
    Optional<Product> findBySlugAndActiveTrue(String slug);

    @EntityGraph(attributePaths = {"brand", "color"})
    List<Product> findTop8ByCategoryIdAndIdNotAndActiveTrueOrderByBestsellerDescReviewCountDesc(Long categoryId, Long excludeId);

    long countByActiveTrue();

    long countByStockLessThanEqual(int stock);

    @EntityGraph(attributePaths = {"brand"})
    List<Product> findTop6ByActiveTrueAndStockLessThanEqualOrderByStockAsc(int stock);

    @Query("select count(p) > 0 from Product p where p.slug = :slug and (:id is null or p.id <> :id)")
    boolean slugTaken(String slug, Long id);
}
