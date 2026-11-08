package com.boutique.admin;

import com.boutique.catalog.Product;
import com.boutique.common.Exceptions;
import com.boutique.common.RaQuery;
import com.boutique.common.RaResponse;
import com.boutique.order.CustomerOrder;
import com.boutique.order.OrderItem;
import com.boutique.order.OrderRepository;
import com.boutique.order.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Les commandes sont créées par la boutique ; le back-office les consulte et fait évoluer leur statut. */
@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {

    public record Line(Long id, Long productId, String productName, String brandName, String colorName, String size,
                       BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}

    public record AdminOrder(Long id, String number, OrderStatus status,
                             String email, String firstName, String lastName, String customerName, String phone,
                             String address, String postalCode, String city, String country,
                             BigDecimal subtotal, BigDecimal shipping, BigDecimal total, String note,
                             OffsetDateTime createdAt, OffsetDateTime updatedAt, int itemCount, List<Line> items) {}

    public record OrderUpdate(@NotNull OrderStatus status, String note) {}

    private static final Map<String, String> SORTS = Map.of(
            "id", "id", "number", "number", "status", "status", "total", "total",
            "createdAt", "createdAt", "customerName", "lastName", "email", "email");

    private final OrderRepository orders;

    public OrderAdminController(OrderRepository orders) {
        this.orders = orders;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<AdminOrder>> list(@RequestParam(required = false) String sort,
                                                 @RequestParam(required = false) String range,
                                                 @RequestParam(required = false) String filter) {
        RaQuery q = RaQuery.parse(sort, range, filter);
        List<Long> ids = q.ids();
        Specification<CustomerOrder> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (ids != null) p.add(root.get("id").in(ids));
            String text = q.text("q");
            if (text != null) {
                String like = "%" + text.toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("number")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("lastName")), like)));
            }
            String status = q.text("status");
            if (status != null) p.add(cb.equal(root.get("status"), OrderStatus.valueOf(status)));
            return cb.and(p.toArray(Predicate[]::new));
        };
        Pageable pageable = ids != null ? PageRequest.of(0, Math.max(ids.size(), 1)) : q.pageable(SORTS);
        return RaResponse.list("orders", orders.findAll(spec, pageable), OrderAdminController::toDto);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public AdminOrder one(@PathVariable Long id) {
        return toDto(find(id));
    }

    @PutMapping("/{id}")
    @Transactional
    public AdminOrder update(@PathVariable Long id, @RequestBody OrderUpdate req) {
        if (req.status() == null) throw new Exceptions.BadRequest("Statut obligatoire");
        CustomerOrder o = find(id);
        boolean cancelling = req.status() == OrderStatus.ANNULEE && o.getStatus() != OrderStatus.ANNULEE;
        boolean reopening = req.status() != OrderStatus.ANNULEE && o.getStatus() == OrderStatus.ANNULEE;
        for (OrderItem item : o.getItems()) {
            Product p = item.getProduct();
            if (p == null) continue;
            if (cancelling) p.setStock(p.getStock() + item.getQuantity());
            if (reopening) p.setStock(Math.max(0, p.getStock() - item.getQuantity()));
        }
        o.setStatus(req.status());
        o.setNote(req.note());
        return toDto(orders.saveAndFlush(o));
    }

    private CustomerOrder find(Long id) {
        return orders.findById(id).orElseThrow(() -> new Exceptions.NotFound("Commande " + id + " introuvable"));
    }

    static AdminOrder toDto(CustomerOrder o) {
        List<Line> lines = o.getItems().stream()
                .map(i -> new Line(i.getId(), i.getProduct() == null ? null : i.getProduct().getId(),
                        i.getProductName(), i.getBrandName(), i.getColorName(), i.getSize(),
                        i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                .toList();
        return new AdminOrder(o.getId(), o.getNumber(), o.getStatus(),
                o.getEmail(), o.getFirstName(), o.getLastName(), o.getFirstName() + " " + o.getLastName(), o.getPhone(),
                o.getAddress(), o.getPostalCode(), o.getCity(), o.getCountry(),
                o.getSubtotal(), o.getShipping(), o.getTotal(), o.getNote(),
                o.getCreatedAt(), o.getUpdatedAt(),
                lines.stream().mapToInt(Line::quantity).sum(), lines);
    }
}
