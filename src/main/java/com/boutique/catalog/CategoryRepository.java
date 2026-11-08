package com.boutique.catalog;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends CatalogRepository<Category> {

    Optional<Category> findBySlug(String slug);

    @Query("select c from Category c left join fetch c.parent order by c.position")
    List<Category> findAllWithParent();
}
