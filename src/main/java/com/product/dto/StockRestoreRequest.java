package com.product.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class StockRestoreRequest {
    private UUID productId;
    private int quantity;
}

