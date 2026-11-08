package com.boutique.catalog;

import com.boutique.catalog.CatalogDtos.PageResponse;
import com.boutique.catalog.CatalogDtos.ProductDetail;
import com.boutique.catalog.CatalogDtos.ProductSummary;
import com.boutique.common.Exceptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final int MAX_PAGE_SIZE = 96;

    private final ProductRepository products;
    private final CategoryService categories;

    public ProductService(ProductRepository products, CategoryService categories) {
        this.products = products;
        this.categories = categories;
    }

    public PageResponse<ProductSummary> search(ProductFilter filter, String sort, int page, int size) {
        var categoryIds = categories.resolve(filter.category(), filter.gender());
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<Product> result = products.findAll(
                ProductSpecifications.storefront(filter, categoryIds),
                PageRequest.of(Math.max(page, 0), safeSize, toSort(sort)));
        return new PageResponse<>(
                result.getContent().stream().map(ProductMapper::summary).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public ProductDetail bySlug(String slug) {
        Product p = products.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new Exceptions.NotFound("Produit introuvable : " + slug));
        var related = products
                .findTop8ByCategoryIdAndIdNotAndActiveTrueOrderByBestsellerDescReviewCountDesc(p.getCategory().getId(), p.getId())
                .stream().map(ProductMapper::summary).toList();
        return ProductMapper.detail(p, categories.breadcrumb(p.getCategory().getId()), related);
    }

    static Sort toSort(String sort) {
        Sort tieBreak = Sort.by(Sort.Direction.DESC, "id");
        return switch (sort == null ? "popularity" : sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt").and(tieBreak);
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price").and(tieBreak);
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price").and(tieBreak);
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating", "reviewCount").and(tieBreak);
            case "discount" -> Sort.by(Sort.Order.desc("originalPrice").nullsLast()).and(tieBreak);
            default -> Sort.by(Sort.Direction.DESC, "bestseller", "reviewCount").and(tieBreak);
        };
    }
}
