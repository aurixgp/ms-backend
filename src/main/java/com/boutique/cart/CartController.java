package com.boutique.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    public record AddItem(@NotNull Long productId, @Size(max = 20) String size, @Min(1) @Max(10) Integer quantity) {}
    public record UpdateQuantity(@Min(0) @Max(10) int quantity) {}

    private final CartService carts;

    public CartController(CartService carts) {
        this.carts = carts;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartView create() {
        return carts.create();
    }

    @GetMapping("/{id}")
    public CartView get(@PathVariable UUID id) {
        return carts.get(id);
    }

    @PostMapping("/{id}/items")
    public CartView add(@PathVariable UUID id, @Valid @RequestBody AddItem req) {
        return carts.addItem(id, req.productId(), req.size(), req.quantity() == null ? 1 : req.quantity());
    }

    /** lineId = identifiant de la ligne du panier (un même produit peut figurer en deux tailles). */
    @PatchMapping("/{id}/items/{lineId}")
    public CartView update(@PathVariable UUID id, @PathVariable Long lineId, @Valid @RequestBody UpdateQuantity req) {
        return carts.updateQuantity(id, lineId, req.quantity());
    }

    @DeleteMapping("/{id}/items/{lineId}")
    public CartView remove(@PathVariable UUID id, @PathVariable Long lineId) {
        return carts.updateQuantity(id, lineId, 0);
    }
}
