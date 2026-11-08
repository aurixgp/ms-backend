package com.boutique.admin;

import com.boutique.catalog.ProductRepository;
import com.boutique.order.CustomerOrder;
import com.boutique.order.OrderRepository;
import com.boutique.order.OrderStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private static final int LOW_STOCK = 5;
    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");

    public record Day(LocalDate date, long orders, BigDecimal revenue) {}
    public record LowStock(Long id, String name, String brand, int stock) {}
    public record Summary(long products, long activeProducts, long lowStockCount,
                          long orders, long pendingOrders, BigDecimal revenue,
                          List<Day> last30Days, List<OrderAdminController.AdminOrder> recentOrders,
                          List<LowStock> lowStock) {}

    private final ProductRepository products;
    private final OrderRepository orders;

    public DashboardController(ProductRepository products, OrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Summary summary() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate first = today.minusDays(29);
        Map<LocalDate, long[]> counts = new TreeMap<>();
        Map<LocalDate, BigDecimal> revenue = new TreeMap<>();
        for (LocalDate d = first; !d.isAfter(today); d = d.plusDays(1)) {
            counts.put(d, new long[]{0});
            revenue.put(d, BigDecimal.ZERO);
        }
        OffsetDateTime from = first.atStartOfDay(ZONE).toOffsetDateTime();
        for (CustomerOrder o : orders.findByCreatedAtAfter(from)) {
            if (o.getStatus() == OrderStatus.ANNULEE) continue;
            LocalDate d = o.getCreatedAt().atZoneSameInstant(ZONE).toLocalDate();
            if (!counts.containsKey(d)) continue;
            counts.get(d)[0]++;
            revenue.merge(d, o.getTotal(), BigDecimal::add);
        }
        List<Day> days = counts.keySet().stream().map(d -> new Day(d, counts.get(d)[0], revenue.get(d))).toList();

        return new Summary(
                products.count(),
                products.countByActiveTrue(),
                products.countByStockLessThanEqual(LOW_STOCK),
                orders.count(),
                orders.countByStatus(OrderStatus.EN_ATTENTE) + orders.countByStatus(OrderStatus.PAYEE),
                orders.revenue(),
                days,
                orders.findTop5ByOrderByCreatedAtDesc().stream().map(OrderAdminController::toDto).toList(),
                products.findTop6ByActiveTrueAndStockLessThanEqualOrderByStockAsc(LOW_STOCK).stream()
                        .map(p -> new LowStock(p.getId(), p.getName(), p.getBrand().getName(), p.getStock()))
                        .toList());
    }
}
