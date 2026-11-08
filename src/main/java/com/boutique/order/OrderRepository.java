package com.boutique.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long>, JpaSpecificationExecutor<CustomerOrder> {

    @Query(value = "select nextval('order_number_seq')", nativeQuery = true)
    long nextNumber();

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.total), 0) from CustomerOrder o where o.status <> com.boutique.order.OrderStatus.ANNULEE")
    BigDecimal revenue();

    List<CustomerOrder> findTop5ByOrderByCreatedAtDesc();

    List<CustomerOrder> findByCreatedAtAfter(OffsetDateTime from);
}
