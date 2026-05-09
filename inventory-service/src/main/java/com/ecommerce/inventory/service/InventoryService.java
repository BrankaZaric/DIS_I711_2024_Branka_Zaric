package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {
        if (inventoryRepository.existsByProductSku(request.getProductSku())) {
            throw new RuntimeException("Inventory already exists for SKU: " + request.getProductSku());
        }

        Inventory inventory = new Inventory();
        inventory.setProductSku(request.getProductSku());
        inventory.setQuantity(request.getQuantity());
        inventory.setReservedQuantity(0);
        inventory.setAvailableQuantity(request.getQuantity());
        inventory.setLastUpdated(LocalDateTime.now());

        Inventory saved = inventoryRepository.save(inventory);
        return mapToResponse(saved);
    }

    public InventoryResponse getInventoryBySku(String sku) {
        Inventory inventory = inventoryRepository.findByProductSku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));
        return mapToResponse(inventory);
    }

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryResponse updateStock(String sku, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductSku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));

        inventory.setQuantity(quantity);
        inventory.setLastUpdated(LocalDateTime.now());

        Inventory updated = inventoryRepository.save(inventory);
        return mapToResponse(updated);
    }

    @Transactional
    public void reserveStock(String sku, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductSku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for SKU: " + sku);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void releaseStock(String sku, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductSku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));

        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
    }

    public boolean isInStock(String sku, Integer quantity) {
        return inventoryRepository.findByProductSku(sku)
                .map(inv -> inv.getAvailableQuantity() >= quantity)
                .orElse(false);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductSku(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}