package com.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StockCheckRequest {
    private UUID productId;
    private int quantity;
}

