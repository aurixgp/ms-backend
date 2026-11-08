package com.boutique.admin;

import com.boutique.catalog.*;
import com.boutique.common.Exceptions;
import com.boutique.common.RaQuery;
import com.boutique.common.RaResponse;
import com.boutique.common.Slugs;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/products")
public class ProductAdminController {

    /** Format compatible avec ImageInput de react-admin. */
    public record AdminImage(String src, String title) {}

    public record AdminProduct(
            Long id,
            @NotBlank String name,
            String slug,
            String description,
            @NotNull Long brandId,
            @NotNull Long categoryId,
            @NotNull Long colorId,
            Long materialId,
            Long patternId,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            BigDecimal originalPrice,
            @NotNull @Min(0) Integer stock,
            String sizes,
            boolean bestseller,
            boolean newArrival,
            boolean exclusive,
            boolean active,
            BigDecimal rating,
            Integer reviewCount,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<AdminImage> images
    ) {}

    private static final Map<String, String> SORTS = Map.of(
            "id", "id", "name", "name", "price", "price", "stock", "stock",
            "createdAt", "createdAt", "updatedAt", "updatedAt", "active", "active",
            "brandId", "brand.name", "categoryId", "category.name");

    private final ProductRepository products;
    private final BrandRepository brands;
    private final CategoryRepository categoryRepo;
    private final ColorRepository colors;
    private final MaterialRepository materials;
    private final PatternRepository patterns;
    private final CategoryService categoryService;

    public ProductAdminController(ProductRepository products, BrandRepository brands, CategoryRepository categoryRepo,
                                  ColorRepository colors, MaterialRepository materials, PatternRepository patterns,
                                  CategoryService categoryService) {
        this.products = products;
        this.brands = brands;
        this.categoryRepo = categoryRepo;
        this.colors = colors;
        this.materials = materials;
        this.patterns = patterns;
        this.categoryService = categoryService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<AdminProduct>> list(@RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) String range,
                                                   @RequestParam(required = false) String filter) {
        RaQuery q = RaQuery.parse(sort, range, filter);
        List<Long> ids = q.ids();
        Long categoryId = q.longValue("categoryId");
        Set<Long> categoryIds = categoryId == null ? null : categoryService.descendantIds(categoryId);

        Specification<Product> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (ids != null) p.add(root.get("id").in(ids));
            String text = q.text("q");
            if (text != null) {
                String like = "%" + text.toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("slug")), like)));
            }
            Long brandId = q.longValue("brandId");
            if (brandId != null) p.add(cb.equal(root.get("brand").get("id"), brandId));
            if (categoryIds != null) p.add(root.get("category").get("id").in(categoryIds));
            Long colorId = q.longValue("colorId");
            if (colorId != null) p.add(cb.equal(root.get("color").get("id"), colorId));
            Boolean active = q.bool("active");
            if (active != null) p.add(cb.equal(root.get("active"), active));
            if (Boolean.TRUE.equals(q.bool("onSale"))) p.add(cb.isNotNull(root.get("originalPrice")));
            if (Boolean.TRUE.equals(q.bool("lowStock"))) p.add(cb.lessThanOrEqualTo(root.get("stock"), 5));
            return cb.and(p.toArray(Predicate[]::new));
        };
        Pageable pageable = ids != null ? PageRequest.of(0, Math.max(ids.size(), 1)) : q.pageable(SORTS);
        return RaResponse.list("products", products.findAll(spec, pageable), ProductAdminController::toDto);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public AdminProduct one(@PathVariable Long id) {
        return toDto(find(id));
    }

    @PostMapping
    @Transactional
    public AdminProduct create(@Valid @RequestBody AdminProduct dto) {
        Product p = new Product();
        apply(dto, p);
        return toDto(products.saveAndFlush(p));
    }

    @PutMapping("/{id}")
    @Transactional
    public AdminProduct update(@PathVariable Long id, @Valid @RequestBody AdminProduct dto) {
        Product p = find(id);
        apply(dto, p);
        return toDto(products.saveAndFlush(p));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public AdminProduct delete(@PathVariable Long id) {
        Product p = find(id);
        AdminProduct dto = toDto(p);
        products.delete(p);
        products.flush();
        return dto;
    }

    private void apply(AdminProduct d, Product p) {
        if (d.originalPrice() != null && d.originalPrice().compareTo(d.price()) <= 0) {
            throw new Exceptions.BadRequest("Le prix barré doit être supérieur au prix de vente");
        }
        String slug = Slugs.orGenerate(d.slug(), d.name());
        if (products.slugTaken(slug, p.getId())) {
            slug = slug + "-" + System.currentTimeMillis() % 100000;
        }
        p.setName(d.name().trim());
        p.setSlug(slug);
        p.setDescription(d.description());
        p.setBrand(brands.findById(d.brandId()).orElseThrow(() -> new Exceptions.BadRequest("Marque inconnue")));
        p.setCategory(categoryRepo.findById(d.categoryId()).orElseThrow(() -> new Exceptions.BadRequest("Catégorie inconnue")));
        p.setColor(colors.findById(d.colorId()).orElseThrow(() -> new Exceptions.BadRequest("Couleur inconnue")));
        p.setMaterial(d.materialId() == null ? null : materials.findById(d.materialId()).orElse(null));
        p.setPattern(d.patternId() == null ? null : patterns.findById(d.patternId()).orElse(null));
        p.setPrice(d.price());
        p.setOriginalPrice(d.originalPrice());
        p.setStock(d.stock());
        p.setSizes(d.sizes() == null || d.sizes().isBlank() ? null
                : String.join(",", java.util.Arrays.stream(d.sizes().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList()));
        p.setBestseller(d.bestseller());
        p.setNewArrival(d.newArrival());
        p.setExclusive(d.exclusive());
        p.setActive(d.active());

        List<String[]> images = new ArrayList<>();
        if (d.images() != null) {
            for (AdminImage img : d.images()) {
                if (img != null && img.src() != null && !img.src().isBlank()) {
                    images.add(new String[]{img.src(), img.title() == null || img.title().isBlank() ? p.getName() : img.title()});
                }
            }
        }
        p.replaceImages(images);
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> new Exceptions.NotFound("Produit " + id + " introuvable"));
    }

    static AdminProduct toDto(Product p) {
        return new AdminProduct(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getBrand().getId(), p.getCategory().getId(), p.getColor().getId(),
                p.getMaterial() == null ? null : p.getMaterial().getId(),
                p.getPattern() == null ? null : p.getPattern().getId(),
                p.getPrice(), p.getOriginalPrice(), p.getStock(), p.getSizes(),
                p.isBestseller(), p.isNewArrival(), p.isExclusive(), p.isActive(),
                p.getRating(), p.getReviewCount(), p.getCreatedAt(), p.getUpdatedAt(),
                p.getImages().stream().map(i -> new AdminImage(i.getUrl(), i.getAlt())).toList());
    }
}
