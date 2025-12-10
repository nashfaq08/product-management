package com.product.service;

import com.product.dto.ProductRequest;
import com.product.dto.StockCheckRequest;
import com.product.dto.StockDeductRequest;
import com.product.dto.StockRestoreRequest;
import com.product.dto.response.ApiResponse;
import com.product.dto.response.PagedResponse;
import com.product.dto.response.ProductResponse;
import com.product.entities.Product;
import com.product.exception.ResourceNotFoundException;
import com.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @CacheEvict(value = {
            "productById",
            "productList",
            "productSearch",
            "productPriceFilter",
            "productAvailability"
    }, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product with name={}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        productRepository.save(product);

        log.info("Product created successfully with id={}", product.getId());
        return toDTO(product);
    }

    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all active (non-deleted) products");

        List<ProductResponse> products = productRepository.findByDeletedFalse()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        log.info("Fetched {} products", products.size());
        return products;
    }

    @Cacheable(value = "productById", key = "#id")
    public ProductResponse getProduct(UUID id) {
        log.info("Fetching product with id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with id={} not found", id);
                    return new ResourceNotFoundException("Product not found");
                });

        if (product.isDeleted()) {
            log.warn("Product with id={} is deleted", id);
            throw new ResourceNotFoundException("Product not available");
        }

        log.info("Product found: id={}", id);
        return toDTO(product);
    }

    @CacheEvict(value = {
            "productById",
            "productList",
            "productSearch",
            "productPriceFilter",
            "productAvailability"
    }, allEntries = true)
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        log.info("Updating product with id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with id={} not found for update", id);
                    return new ResourceNotFoundException("Product not found");
                });

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        productRepository.save(product);

        log.info("Product updated successfully: id={}", id);
        return toDTO(product);
    }

    public ProductResponse updateProductPartial(UUID id, ProductRequest request) {
        log.info("Partial update (PATCH) for product id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        boolean updated = false;

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            product.setName(request.getName());
            updated = true;
        }

        if (request.getDescription() != null) {
            if (request.getDescription().isBlank()) {
                throw new IllegalArgumentException("Description cannot be blank");
            }
            product.setDescription(request.getDescription());
            updated = true;
        }

        if (request.getPrice() != null) {
            if (request.getPrice() <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0");
            }
            product.setPrice(request.getPrice());
            updated = true;
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            product.setQuantity(request.getQuantity());
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("No valid fields provided to update");
        }

        productRepository.save(product);
        log.info("Product patched successfully: id={}", id);

        return toDTO(product);
    }

    @CacheEvict(value = {
            "productById",
            "productList",
            "productSearch",
            "productPriceFilter",
            "productAvailability"
    }, allEntries = true)
    public ResponseEntity<ApiResponse> softDeleteProduct(UUID id) {
        log.info("Soft deleting product with id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with id={} not found for soft delete", id);
                    return new ResourceNotFoundException("Product not found");
                });

        product.setDeleted(true);
        productRepository.save(product);

        log.info("Product soft deleted: id={}", id);

        ApiResponse response = new ApiResponse(
                "Product deleted successfully",
                id.toString()
        );

        return ResponseEntity.ok(response);
    }

    @Cacheable(value = "productSearch", key = "#name")
    public List<ProductResponse> searchByName(String name) {
        log.info("Searching products by name containing '{}'", name);

        List<ProductResponse> results = productRepository
                .findByNameContainingIgnoreCaseAndDeletedFalse(name)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        log.info("Found {} products matching name '{}'", results.size(), name);
        return results;
    }

    @Cacheable(value = "productPriceFilter", key = "{#min, #max}")
    public List<ProductResponse> filterByPrice(Double min, Double max) {
        log.info("Filtering products by price range {} - {}", min, max);

        List<ProductResponse> results = productRepository
                .findByPriceBetweenAndDeletedFalse(min, max)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        log.info("Found {} products in price range {} - {}", results.size(), min, max);
        return results;
    }

    @Cacheable(value = "productAvailability")
    public List<ProductResponse> filterAvailable() {
        log.info("Fetching only available products (quantity > 0)");

        List<ProductResponse> results = productRepository
                .findByQuantityGreaterThanAndDeletedFalse(0)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        log.info("Found {} available products", results.size());
        return results;
    }

    public void validateStock(List<StockCheckRequest> requests) {

        log.info("Validating stock for {} products", requests.size());

        for (StockCheckRequest req : requests) {

            log.info("Checking stock for product={}, requestedQuantity={}", req.getProductId(), req.getQuantity());

            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> {
                        log.error("Product {} not found", req.getProductId());
                        return new RuntimeException("Product not found: " + req.getProductId());
                    });

            if (product.isDeleted()) {
                log.error("Product {} is deleted", product.getId());
                throw new RuntimeException("Product is not available: " + product.getId());
            }

            if (product.getQuantity() < req.getQuantity()) {
                log.error("Insufficient stock for product {}. Available={}, Requested={}",
                        product.getId(), product.getQuantity(), req.getQuantity());
                throw new RuntimeException(
                        "Insufficient stock for product " + product.getName()
                                + ". Available: " + product.getQuantity()
                                + ", Required: " + req.getQuantity()
                );
            }
        }

        log.info("Stock validation successful.");
    }

    @Transactional
    public void deductStock(List<StockDeductRequest> requests) {

        log.info("Deducting stock for {} products", requests.size());

        for (StockDeductRequest req : requests) {

            log.info("Deducting stock for product={}, quantity={}", req.getProductId(), req.getQuantity());

            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> {
                        log.error("Product {} not found", req.getProductId());
                        return new RuntimeException("Product not found: " + req.getProductId());
                    });

            if (product.getQuantity() < req.getQuantity()) {
                log.error("Cannot deduct stock. Insufficient stock for product {}. Available={}, Requested={}",
                        product.getId(), product.getQuantity(), req.getQuantity());
                throw new RuntimeException(
                        "Insufficient stock to deduct for product " + product.getName()
                );
            }

            // Deduct inventory
            product.setQuantity(product.getQuantity() - req.getQuantity());

            productRepository.save(product);

            log.info("Stock updated. Product={}, RemainingQuantity={}",
                    product.getId(), product.getQuantity());
        }

        log.info("Stock deduction completed successfully.");
    }

    @Transactional
    public void restoreStock(List<StockRestoreRequest> requests) {
        for (StockRestoreRequest r : requests) {
            Product product = productRepository.findById(r.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setQuantity(product.getQuantity() + r.getQuantity());
            productRepository.save(product);
        }
    }

    @Cacheable(value = "productList", key = "{#page, #size, #sortBy, #sortDir}")
    public PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByDeletedFalse(pageable);

        List<ProductResponse> products = productPage.getContent()
                .stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getQuantity()
                ))
                .toList();

        return new PagedResponse<>(
                products,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    private ProductResponse toDTO(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}