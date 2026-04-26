package com.itm.eco_store.checkout.infrastructure.adapter.out.persistence;

import com.itm.eco_store.checkout.application.port.out.CheckoutRepositoryPort;
import com.itm.eco_store.checkout.domain.model.Checkout;
import com.itm.eco_store.checkout.domain.model.CheckoutItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PostgresCheckoutRepositoryAdapter implements CheckoutRepositoryPort {

    private final CheckoutJpaRepository repository;
    private final CheckoutItemJpaRepository itemRepository;

    public PostgresCheckoutRepositoryAdapter(CheckoutJpaRepository repository, CheckoutItemJpaRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Checkout save(Checkout checkout) {
        CheckoutJpaEntity entity = new CheckoutJpaEntity(
                checkout.getId(),
                checkout.getCartId(),
                checkout.getTotalAmount(),
                checkout.getCurrency(),
                checkout.getPaymentProvider(),
                checkout.getPaymentReference(),
                checkout.getStatus(),
                checkout.getCreatedAt()
        );
        CheckoutJpaEntity saved = repository.save(entity);
        itemRepository.deleteByCheckoutId(saved.getId());
        if (checkout.getItems() != null && !checkout.getItems().isEmpty()) {
            List<CheckoutItemJpaEntity> items = new ArrayList<>();
            for (CheckoutItem item : checkout.getItems()) {
                items.add(new CheckoutItemJpaEntity(
                        saved,
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal(),
                        item.getCurrency()
                ));
            }
            itemRepository.saveAll(items);
        }
        List<CheckoutItem> detail = mapItems(saved.getId());
        return new Checkout(
                saved.getId(),
                saved.getCartId(),
                saved.getTotalAmount(),
                saved.getCurrency(),
                saved.getPaymentProvider(),
                saved.getPaymentReference(),
                null,
                saved.getStatus(),
                saved.getCreatedAt(),
                detail
        );
    }

    @Override
    public Optional<Checkout> findByPaymentReference(String paymentReference) {
        return repository.findByPaymentReference(paymentReference)
                .map(saved -> new Checkout(
                        saved.getId(),
                        saved.getCartId(),
                        saved.getTotalAmount(),
                        saved.getCurrency(),
                        saved.getPaymentProvider(),
                        saved.getPaymentReference(),
                        null,
                        saved.getStatus(),
                        saved.getCreatedAt(),
                        mapItems(saved.getId())
                ));
    }

    private List<CheckoutItem> mapItems(Long checkoutId) {
        return itemRepository.findByCheckoutId(checkoutId).stream()
                .map(item -> new CheckoutItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal(),
                        item.getCurrency()
                ))
                .toList();
    }
}
