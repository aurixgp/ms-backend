package com.boutique.order;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.EN_ATTENTE;

    @Column(nullable = false) private String email;
    @Column(name = "first_name", nullable = false) private String firstName;
    @Column(name = "last_name", nullable = false) private String lastName;
    private String phone;
    @Column(nullable = false) private String address;
    @Column(name = "postal_code", nullable = false) private String postalCode;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private String country;

    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal shipping = BigDecimal.ZERO;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal total = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    private String note;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {}

    CustomerOrder(String number, String email, String firstName, String lastName, String phone,
                  String address, String postalCode, String city, String country) {
        this.number = number;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    void setTotals(BigDecimal subtotal, BigDecimal shipping) {
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.total = subtotal.add(shipping);
    }

    @PreUpdate
    void touch() { updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public String getNumber() { return number; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getPostalCode() { return postalCode; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShipping() { return shipping; }
    public BigDecimal getTotal() { return total; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return items; }
}
