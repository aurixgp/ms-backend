package com.boutique.order;

import com.boutique.cart.Cart;
import com.boutique.cart.CartItem;
import com.boutique.cart.CartService;
import com.boutique.cart.ShippingPolicy;
import com.boutique.catalog.Product;
import com.boutique.common.Exceptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderService {

    public record Checkout(UUID cartId, String email, String firstName, String lastName, String phone,
                           String address, String postalCode, String city, String country) {}

    public record Confirmation(String number, String email, BigDecimal total, int itemCount) {}

    private final OrderRepository orders;
    private final CartService carts;
    private final ShippingPolicy shipping;

    public OrderService(OrderRepository orders, CartService carts, ShippingPolicy shipping) {
        this.orders = orders;
        this.carts = carts;
        this.shipping = shipping;
    }

    @Transactional
    public Confirmation checkout(Checkout c) {
        Cart cart = carts.find(c.cartId());
        if (cart.getItems().isEmpty()) throw new Exceptions.BadRequest("Le panier est vide");

        CustomerOrder order = new CustomerOrder(
                "CMD-" + orders.nextNumber(), c.email().trim().toLowerCase(), c.firstName().trim(), c.lastName().trim(),
                c.phone(), c.address().trim(), c.postalCode().trim(), c.city().trim(), c.country().trim());

        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;
        for (CartItem line : cart.getItems()) {
            Product p = line.getProduct();
            if (!p.isActive()) throw new Exceptions.BadRequest("« " + p.getName() + " » n'est plus disponible");
            if (p.getStock() < line.getQuantity()) {
                throw new Exceptions.BadRequest("Stock insuffisant pour « " + p.getName() + " » (" + p.getStock() + " restant)");
            }
            p.setStock(p.getStock() - line.getQuantity());
            OrderItem item = new OrderItem(order, p, line.getSize(), line.getQuantity());
            order.getItems().add(item);
            subtotal = subtotal.add(item.getLineTotal());
            count += line.getQuantity();
        }
        order.setTotals(subtotal, shipping.shippingFor(subtotal));
        orders.save(order);
        carts.delete(cart);
        return new Confirmation(order.getNumber(), order.getEmail(), order.getTotal(), count);
    }
}
