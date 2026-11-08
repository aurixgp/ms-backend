package com.boutique.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id")
    private Pattern pattern;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private boolean bestseller;

    @Column(name = "is_new", nullable = false)
    private boolean newArrival;

    @Column(nullable = false)
    private boolean exclusive;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int stock;

    /** Tailles, pointures ou contenances, séparées par des virgules. */
    private String sizes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ProductImage> images = new ArrayList<>();

    public Integer getDiscountPercent() {
        if (originalPrice == null || originalPrice.signum() == 0) return null;
        return BigDecimal.ONE
                .subtract(price.divide(originalPrice, 4, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    /** Remplace la galerie en conservant l'ordre reçu. */
    public void replaceImages(List<String[]> urlAndAlt) {
        images.clear();
        int pos = 0;
        for (String[] img : urlAndAlt) {
            images.add(new ProductImage(this, img[0], img[1], pos++));
        }
    }

    @PreUpdate
    void touch() { updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public Pattern getPattern() { return pattern; }
    public void setPattern(Pattern pattern) { this.pattern = pattern; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isBestseller() { return bestseller; }
    public void setBestseller(boolean bestseller) { this.bestseller = bestseller; }
    public boolean isNewArrival() { return newArrival; }
    public void setNewArrival(boolean newArrival) { this.newArrival = newArrival; }
    public boolean isExclusive() { return exclusive; }
    public void setExclusive(boolean exclusive) { this.exclusive = exclusive; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getStock() { return stock; }
    public String getSizes() { return sizes; }
    public void setSizes(String sizes) { this.sizes = sizes; }

    public java.util.List<String> getSizeList() {
        if (sizes == null || sizes.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(sizes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
    public void setStock(int stock) { this.stock = stock; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<ProductImage> getImages() { return images; }
}
