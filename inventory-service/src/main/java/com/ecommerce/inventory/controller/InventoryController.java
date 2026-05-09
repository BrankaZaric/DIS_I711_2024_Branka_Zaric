package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        List<InventoryResponse> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryResponse> getInventoryBySku(@PathVariable String sku) {
        InventoryResponse response = inventoryService.getInventoryBySku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sku}/check")
    public ResponseEntity<Boolean> checkStock(@PathVariable String sku, @RequestParam Integer quantity) {
        boolean inStock = inventoryService.isInStock(sku, quantity);
        return ResponseEntity.ok(inStock);
    }

    @PatchMapping("/{sku}/stock")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable String sku,
            @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.updateStock(sku, quantity);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sku}/reserve")
    public ResponseEntity<Void> reserveStock(@PathVariable String sku, @RequestParam Integer quantity) {
        inventoryService.reserveStock(sku, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sku}/release")
    public ResponseEntity<Void> releaseStock(@PathVariable String sku, @RequestParam Integer quantity) {
        inventoryService.releaseStock(sku, quantity);
        return ResponseEntity.ok().build();
    }
}