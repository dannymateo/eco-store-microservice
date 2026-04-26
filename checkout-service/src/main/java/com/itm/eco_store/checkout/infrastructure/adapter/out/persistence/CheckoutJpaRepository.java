package com.itm.eco_store.checkout.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckoutJpaRepository extends JpaRepository<CheckoutJpaEntity, Long> {
    Optional<CheckoutJpaEntity> findByPaymentReference(String paymentReference);
}
