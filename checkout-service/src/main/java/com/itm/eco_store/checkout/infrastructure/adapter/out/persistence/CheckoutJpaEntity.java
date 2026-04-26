package com.itm.eco_store.checkout.infrastructure.adapter.out.persistence;

import com.itm.eco_store.checkout.domain.model.CheckoutStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checkouts")
public class CheckoutJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String cartId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(nullable = false, length = 40)
    private String paymentProvider;

    @Column(nullable = false, length = 120)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckoutStatus status;

    @Column(nullable = false)
    private Instant createdAt;
    
    @OneToMany(mappedBy = "checkout", orphanRemoval = true)
    private List<CheckoutItemJpaEntity> items = new ArrayList<>();

    public CheckoutJpaEntity() {
    }

    public CheckoutJpaEntity(Long id, String cartId, BigDecimal totalAmount, String currency, String paymentProvider,
                             String paymentReference, CheckoutStatus status, Instant createdAt) {
        this.id = id;
        this.cartId = cartId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentProvider = paymentProvider;
        this.paymentReference = paymentReference;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public CheckoutStatus getStatus() { return status; }
    public void setStatus(CheckoutStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<CheckoutItemJpaEntity> getItems() { return items; }
    public void setItems(List<CheckoutItemJpaEntity> items) { this.items = items; }
}
