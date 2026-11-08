package com.boutique.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String url;

    private String alt;

    @Column(nullable = false)
    private int position;

    protected ProductImage() {}

    ProductImage(Product product, String url, String alt, int position) {
        this.product = product;
        this.url = url;
        this.alt = alt;
        this.position = position;
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getAlt() { return alt; }
    public int getPosition() { return position; }
}
