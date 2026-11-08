package com.boutique.order;

import com.boutique.catalog.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    // Nom, marque et prix sont copiés : la commande reste lisible même si le produit change ou disparaît
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "color_name")
    private String colorName;

    private String size;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    protected OrderItem() {}

    OrderItem(CustomerOrder order, Product product, String size, int quantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.brandName = product.getBrand().getName();
        this.colorName = product.getColor().getName();
        this.size = size == null || size.isBlank() ? null : size;
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getProductName() { return productName; }
    public String getBrandName() { return brandName; }
    public String getColorName() { return colorName; }
    public String getSize() { return size; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
