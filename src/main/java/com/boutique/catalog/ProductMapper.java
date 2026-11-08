package com.boutique.catalog;

import com.boutique.catalog.CatalogDtos.*;

import java.util.ArrayList;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {}

    public static List<String> badges(Product p) {
        List<String> b = new ArrayList<>();
        if (p.isNewArrival()) b.add("NOUVEAU");
        if (p.isBestseller()) b.add("MEILLEURE_VENTE");
        if (p.getOriginalPrice() != null) b.add("PROMO");
        if (p.getRating().doubleValue() >= 4.6 && p.getReviewCount() >= 10) b.add("MEILLEURE_NOTE");
        if (p.isExclusive()) b.add("EXCLUSIVITE");
        if (p.getBrand().isPremium()) b.add("LUXE");
        return b;
    }

    private static BrandRef brand(Product p) {
        return new BrandRef(p.getBrand().getSlug(), p.getBrand().getName(), p.getBrand().getLogoUrl());
    }

    public static ProductSummary summary(Product p) {
        String image = p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl();
        return new ProductSummary(
                p.getId(), p.getSlug(), p.getName(),
                brand(p),
                new ColorRef(p.getColor().getSlug(), p.getColor().getName(), p.getColor().getHex()),
                p.getCategory().getSlug(), p.getCategory().getUniverse(),
                p.getPrice(), p.getOriginalPrice(), p.getDiscountPercent(),
                p.getRating(), p.getReviewCount(), p.getSizeList(),
                badges(p), image, p.getBrand().isPremium());
    }

    public static ProductDetail detail(Product p, List<Ref> breadcrumb, List<ProductSummary> related) {
        return new ProductDetail(
                p.getId(), p.getSlug(), p.getName(), p.getDescription(),
                brand(p),
                new ColorRef(p.getColor().getSlug(), p.getColor().getName(), p.getColor().getHex()),
                p.getMaterial() == null ? null : new Ref(p.getMaterial().getSlug(), p.getMaterial().getName()),
                p.getPattern() == null ? null : new Ref(p.getPattern().getSlug(), p.getPattern().getName()),
                p.getCategory().getUniverse(),
                p.getPrice(), p.getOriginalPrice(), p.getDiscountPercent(),
                p.getRating(), p.getReviewCount(), p.getStock(), p.getSizeList(),
                badges(p),
                p.getImages().stream().map(i -> new Image(i.getUrl(), i.getAlt())).toList(),
                p.getBrand().isPremium(),
                breadcrumb, related);
    }
}
