package com.boutique.cart;

import com.boutique.catalog.CatalogDtos.ProductSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartView(UUID id, List<Line> items, int itemCount,
                       BigDecimal subtotal, BigDecimal shipping, BigDecimal total,
                       BigDecimal remainingForFreeShipping) {

    public record Line(Long id, ProductSummary product, String size, int quantity, BigDecimal lineTotal) {}
}
