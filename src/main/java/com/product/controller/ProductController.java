package com.product.controller;

import com.product.dto.*;
import com.product.dto.response.ApiResponse;
import com.product.dto.response.PagedResponse;
import com.product.dto.response.ProductResponse;
import com.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> patch(@PathVariable UUID id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProductPartial(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        return productService.softDeleteProduct(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN')")
    public ResponseEntity<ProductResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN')")
    public ResponseEntity<PagedResponse<ProductResponse>> getPagedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy, sortDir));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
    public List<ProductResponse> search(@RequestParam String name) {
        return productService.searchByName(name);
    }

    @GetMapping("/filter/price")
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
    public List<ProductResponse> priceFilter(@RequestParam Double min, @RequestParam Double max) {
        return productService.filterByPrice(min, max);
    }

    @GetMapping("/filter/available")
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
    public List<ProductResponse> availability() {
        return productService.filterAvailable();
    }

    @PostMapping("/validate-stock")
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
    public ResponseEntity<?> validateStock(@RequestBody List<StockCheckRequest> request) {
        productService.validateStock(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deduct-stock")
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
    public ResponseEntity<?> deductStock(@RequestBody List<StockDeductRequest> request) {
        productService.deductStock(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore-stock")
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
    public ResponseEntity<?> restoreStock(@RequestBody List<StockRestoreRequest> request) {
        productService.restoreStock(request);
        return ResponseEntity.ok().build();
    }
}