package com.boutique.cart;

import com.boutique.catalog.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String size = "";

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {}

    public CartItem(Cart cart, Product product, String size, int quantity) {
        this.cart = cart;
        this.product = product;
        this.size = size == null ? "" : size;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public String getSize() { return size; }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
