package com.itm.eco_store.checkout.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckoutItemJpaRepository extends JpaRepository<CheckoutItemJpaEntity, Long> {
    List<CheckoutItemJpaEntity> findByCheckoutId(Long checkoutId);
    void deleteByCheckoutId(Long checkoutId);
}
