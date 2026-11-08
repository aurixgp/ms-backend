package com.boutique.catalog;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {}

    public record Ref(String slug, String name) {}

    public record ColorRef(String slug, String name, String hex) {}

    public record BrandRef(String slug, String name, String logoUrl) {}

    public record Image(String url, String alt) {}

    public record ProductSummary(
            Long id, String slug, String name,
            BrandRef brand, ColorRef color, String categorySlug, String universe,
            BigDecimal price, BigDecimal originalPrice, Integer discountPercent,
            BigDecimal rating, int reviewCount, List<String> sizes,
            List<String> badges, String imageUrl, boolean premium
    ) {}

    public record ProductDetail(
            Long id, String slug, String name, String description,
            BrandRef brand, ColorRef color, Ref material, Ref pattern, String universe,
            BigDecimal price, BigDecimal originalPrice, Integer discountPercent,
            BigDecimal rating, int reviewCount, int stock, List<String> sizes,
            List<String> badges, List<Image> images, boolean premium,
            List<Ref> breadcrumb, List<ProductSummary> related
    ) {}

    public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {}

    public record FacetValue(String slug, String name, long count, String hex) {}

    public record Facets(
            List<FacetValue> brands, List<FacetValue> colors,
            List<FacetValue> materials, List<FacetValue> patterns,
            BigDecimal minPrice, BigDecimal maxPrice, long total
    ) {}

    public record CategoryNode(String slug, String name, String universe, List<CategoryNode> children) {}

    public record CategoryPage(Ref category, List<Ref> breadcrumb, Ref parent, List<Ref> children, List<Ref> siblings) {}

    public record SiteInfo(String name, String currency, BigDecimal freeShippingThreshold, BigDecimal shippingFee) {}
}
