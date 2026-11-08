package com.boutique.catalog;

import com.boutique.catalog.CatalogDtos.*;
import com.boutique.common.AppProperties;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API publique de la boutique (lecture seule, sans authentification). */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final ProductService products;
    private final FacetService facets;
    private final CategoryService categories;
    private final AppProperties props;

    public CatalogController(ProductService products, FacetService facets,
                             CategoryService categories, AppProperties props) {
        this.products = products;
        this.facets = facets;
        this.categories = categories;
        this.props = props;
    }

    @GetMapping("/site")
    public SiteInfo site() {
        return new SiteInfo(props.siteName(), props.currency(), props.freeShippingThreshold(), props.shippingFee());
    }

    @GetMapping("/products")
    public PageResponse<ProductSummary> list(@ModelAttribute ProductFilter filter,
                                             @RequestParam(defaultValue = "popularity") String sort,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "48") int size) {
        return products.search(filter, sort, page, size);
    }

    @GetMapping("/products/{slug}")
    public ProductDetail detail(@PathVariable String slug) {
        return products.bySlug(slug);
    }

    @GetMapping("/facets")
    public Facets facets(@ModelAttribute ProductFilter filter) {
        return facets.facets(filter);
    }

    @GetMapping("/categories")
    public List<CategoryNode> tree() {
        return categories.tree();
    }

    @GetMapping("/categories/{slug}")
    public CategoryPage category(@PathVariable String slug) {
        return categories.page(slug);
    }
}
