package com.itm.eco_store.infrastructure.adapter.out.persistence;

import com.itm.eco_store.application.port.out.ProductRepositoryPort;
import com.itm.eco_store.domain.model.PriceInfo;
import com.itm.eco_store.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostgresProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository repository;
    private static final int DEFAULT_STOCK = 0;

    @Override
    public Product save(Product product) {
        return toDomain(repository.save(toEntity(product)));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Product update(Product product) {
        return toDomain(repository.save(toEntity(product)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return name != null && !name.isBlank() && repository.existsByNameIgnoreCase(name.trim());
    }

    @Override
    public boolean existsByNameExcludingId(String name, Long excludeId) {
        return name != null
                && !name.isBlank()
                && repository.existsByNameIgnoreCaseAndIdNot(name.trim(), excludeId);
    }

    private ProductJpaEntity toEntity(Product product) {
        return ProductJpaEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .originalPrice(product.getPriceInfo().getOriginalPrice())
                .discountPercent(product.getPriceInfo().getDiscountPercent())
                .finalPrice(product.getPriceInfo().getFinalPrice())
                .stock(normalizeStock(product.getStock()))
                .build();
    }

    private Product toDomain(ProductJpaEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .priceInfo(new PriceInfo(entity.getOriginalPrice(), entity.getDiscountPercent(), entity.getFinalPrice()))
                .stock(normalizeStock(entity.getStock()))
                .build();
    }

    private Integer normalizeStock(Integer stock) {
        return stock == null ? DEFAULT_STOCK : stock;
    }
}
