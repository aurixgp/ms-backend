package com.boutique.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record CheckoutRequest(
            @NotNull UUID cartId,
            @NotBlank @Email String email,
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @Size(max = 40) String phone,
            @NotBlank @Size(max = 255) String address,
            @NotBlank @Size(max = 20) String postalCode,
            @NotBlank @Size(max = 120) String city,
            @NotBlank @Size(max = 80) String country) {}

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderService.Confirmation checkout(@Valid @RequestBody CheckoutRequest r) {
        return orders.checkout(new OrderService.Checkout(
                r.cartId(), r.email(), r.firstName(), r.lastName(), r.phone(),
                r.address(), r.postalCode(), r.city(), r.country()));
    }
}
