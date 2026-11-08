package com.boutique.cart;

import com.boutique.catalog.Product;
import com.boutique.catalog.ProductMapper;
import com.boutique.catalog.ProductRepository;
import com.boutique.common.Exceptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CartService {

    private static final int MAX_QTY = 10;

    private final CartRepository carts;
    private final ProductRepository products;
    private final ShippingPolicy shipping;

    public CartService(CartRepository carts, ProductRepository products, ShippingPolicy shipping) {
        this.carts = carts;
        this.products = products;
        this.shipping = shipping;
    }

    public CartView create() {
        return view(carts.save(Cart.create()));
    }

    @Transactional(readOnly = true)
    public CartView get(UUID id) {
        return view(find(id));
    }

    public CartView addItem(UUID cartId, Long productId, String size, int quantity) {
        Cart cart = find(cartId);
        Product product = products.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new Exceptions.NotFound("Produit indisponible"));

        List<String> sizes = product.getSizeList();
        String chosen = size == null ? "" : size.trim();
        if (sizes.size() == 1 && chosen.isEmpty()) chosen = sizes.get(0);
        if (!sizes.isEmpty() && !sizes.contains(chosen)) {
            throw new Exceptions.BadRequest("Choisissez une taille disponible");
        }
        if (sizes.isEmpty()) chosen = "";

        final String lineSize = chosen;
        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId) && i.getSize().equals(lineSize))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.setQuantity(Math.min(i.getQuantity() + quantity, MAX_QTY)),
                        () -> cart.getItems().add(new CartItem(cart, product, lineSize, Math.min(quantity, MAX_QTY))));
        cart.touch();
        return view(carts.saveAndFlush(cart));
    }

    public CartView updateQuantity(UUID cartId, Long lineId, int quantity) {
        Cart cart = find(cartId);
        if (quantity <= 0) {
            cart.getItems().removeIf(i -> lineId.equals(i.getId()));
        } else {
            cart.getItems().stream()
                    .filter(i -> lineId.equals(i.getId()))
                    .findFirst()
                    .orElseThrow(() -> new Exceptions.NotFound("Article absent du panier"))
                    .setQuantity(Math.min(quantity, MAX_QTY));
        }
        cart.touch();
        return view(carts.saveAndFlush(cart));
    }

    public Cart find(UUID id) {
        return carts.findById(id).orElseThrow(() -> new Exceptions.NotFound("Panier introuvable"));
    }

    public void delete(Cart cart) {
        carts.delete(cart);
    }

    private CartView view(Cart cart) {
        List<CartView.Line> lines = cart.getItems().stream()
                .map(i -> new CartView.Line(
                        i.getId(),
                        ProductMapper.summary(i.getProduct()),
                        i.getSize(),
                        i.getQuantity(),
                        i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        BigDecimal subtotal = lines.stream().map(CartView.Line::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fee = shipping.shippingFor(subtotal);
        return new CartView(cart.getId(), lines, lines.stream().mapToInt(CartView.Line::quantity).sum(),
                subtotal, fee, subtotal.add(fee), shipping.remainingForFree(subtotal));
    }
}
