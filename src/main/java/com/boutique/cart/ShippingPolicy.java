package com.boutique.cart;

import com.boutique.common.AppProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ShippingPolicy {

    private final AppProperties props;

    public ShippingPolicy(AppProperties props) {
        this.props = props;
    }

    public BigDecimal shippingFor(BigDecimal subtotal) {
        if (subtotal.signum() == 0 || subtotal.compareTo(props.freeShippingThreshold()) >= 0) return BigDecimal.ZERO;
        return props.shippingFee();
    }

    public BigDecimal remainingForFree(BigDecimal subtotal) {
        BigDecimal rest = props.freeShippingThreshold().subtract(subtotal);
        return subtotal.signum() == 0 || rest.signum() <= 0 ? BigDecimal.ZERO : rest;
    }
}
