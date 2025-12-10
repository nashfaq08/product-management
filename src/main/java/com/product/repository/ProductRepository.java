package com.product.repository;

import com.product.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    List<Product> findByPriceBetweenAndDeletedFalse(Double minPrice, Double maxPrice);

    List<Product> findByQuantityGreaterThanAndDeletedFalse(Integer quantity);

    List<Product> findByDeletedFalse();

    Page<Product> findByDeletedFalse(Pageable pageable);
}

