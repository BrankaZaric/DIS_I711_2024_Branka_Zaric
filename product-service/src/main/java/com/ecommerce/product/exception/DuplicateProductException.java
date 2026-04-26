package com.ecommerce.product.exception;

public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException(String sku) {
        super("Product with SKU already exists: " + sku);
    }
}