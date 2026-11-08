package com.boutique.catalog;

import java.math.BigDecimal;
import java.util.List;

/**
 * Filtres du listing public, liés depuis les paramètres de requête.
 * category = rubrique (et ses sous-rubriques), gender = rayon femme/homme, universe = sacs, chaussures, montres…
 */
public record ProductFilter(
        String category,
        String gender,
        String universe,
        List<String> brands,
        List<String> colors,
        List<String> materials,
        List<String> patterns,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal minRating,
        Boolean onSale,
        Boolean newOnly,
        Boolean premium,
        String q
) {
    // Pour les facettes : le compteur d'une marque ne tient pas compte du filtre « marque » lui-même
    public ProductFilter withoutBrands()    { return new ProductFilter(category, gender, universe, null, colors, materials, patterns, minPrice, maxPrice, minRating, onSale, newOnly, premium, q); }
    public ProductFilter withoutColors()    { return new ProductFilter(category, gender, universe, brands, null, materials, patterns, minPrice, maxPrice, minRating, onSale, newOnly, premium, q); }
    public ProductFilter withoutMaterials() { return new ProductFilter(category, gender, universe, brands, colors, null, patterns, minPrice, maxPrice, minRating, onSale, newOnly, premium, q); }
    public ProductFilter withoutPatterns()  { return new ProductFilter(category, gender, universe, brands, colors, materials, null, minPrice, maxPrice, minRating, onSale, newOnly, premium, q); }
    public ProductFilter withoutPrice()     { return new ProductFilter(category, gender, universe, brands, colors, materials, patterns, null, null, minRating, onSale, newOnly, premium, q); }
}
