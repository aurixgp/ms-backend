package com.boutique.cart;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {}

    public static Cart create() {
        Cart c = new Cart();
        c.id = UUID.randomUUID();
        c.createdAt = OffsetDateTime.now();
        c.updatedAt = c.createdAt;
        return c;
    }

    public void touch() { this.updatedAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public List<CartItem> getItems() { return items; }
}
