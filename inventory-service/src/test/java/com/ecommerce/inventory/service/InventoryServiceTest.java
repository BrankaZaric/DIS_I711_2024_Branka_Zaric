package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;
    private InventoryRequest testRequest;

    @BeforeEach
    void setUp() {
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductSku("TEST-SKU-001");
        testInventory.setQuantity(100);
        testInventory.setReservedQuantity(10);
        testInventory.setAvailableQuantity(90);
        testInventory.setLastUpdated(LocalDateTime.now());

        testRequest = new InventoryRequest();
        testRequest.setProductSku("NEW-SKU-001");
        testRequest.setQuantity(50);
    }

    @Test
    void testCreateInventory_Success() {
        // Arrange
        when(inventoryRepository.existsByProductSku(anyString())).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponse response = inventoryService.createInventory(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testInventory.getId(), response.getId());
        assertEquals(testInventory.getProductSku(), response.getProductSku());
        verify(inventoryRepository, times(1)).existsByProductSku(anyString());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testCreateInventory_AlreadyExists() {
        // Arrange
        when(inventoryRepository.existsByProductSku(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.createInventory(testRequest));
        assertTrue(exception.getMessage().contains("Inventory already exists"));
        verify(inventoryRepository, times(1)).existsByProductSku(anyString());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testGetInventoryBySku_Success() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.of(testInventory));

        // Act
        InventoryResponse response = inventoryService.getInventoryBySku("TEST-SKU-001");

        // Assert
        assertNotNull(response);
        assertEquals(testInventory.getProductSku(), response.getProductSku());
        assertEquals(testInventory.getQuantity(), response.getQuantity());
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
    }

    @Test
    void testGetInventoryBySku_NotFound() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.getInventoryBySku("NONEXISTENT"));
        assertTrue(exception.getMessage().contains("Inventory not found"));
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
    }

    @Test
    void testGetAllInventory() {
        // Arrange
        Inventory inventory2 = new Inventory();
        inventory2.setId(2L);
        inventory2.setProductSku("TEST-SKU-002");
        inventory2.setQuantity(200);
        inventory2.setReservedQuantity(20);
        inventory2.setAvailableQuantity(180);
        inventory2.setLastUpdated(LocalDateTime.now());

        List<Inventory> inventoryList = Arrays.asList(testInventory, inventory2);
        when(inventoryRepository.findAll()).thenReturn(inventoryList);

        // Act
        List<InventoryResponse> responses = inventoryService.getAllInventory();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    void testUpdateStock_Success() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponse response = inventoryService.updateStock("TEST-SKU-001", 150);

        // Assert
        assertNotNull(response);
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReserveStock_Success() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        inventoryService.reserveStock("TEST-SKU-001", 20);

        // Assert
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReserveStock_InsufficientStock() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.of(testInventory));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.reserveStock("TEST-SKU-001", 200));
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testReleaseStock_Success() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        inventoryService.releaseStock("TEST-SKU-001", 5);

        // Assert
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testIsInStock_True() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.of(testInventory));

        // Act
        boolean result = inventoryService.isInStock("TEST-SKU-001", 50);

        // Assert
        assertTrue(result);
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
    }

    @Test
    void testIsInStock_False() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.of(testInventory));

        // Act
        boolean result = inventoryService.isInStock("TEST-SKU-001", 200);

        // Assert
        assertFalse(result);
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
    }

    @Test
    void testIsInStock_ProductNotFound() {
        // Arrange
        when(inventoryRepository.findByProductSku(anyString())).thenReturn(Optional.empty());

        // Act
        boolean result = inventoryService.isInStock("NONEXISTENT", 10);

        // Assert
        assertFalse(result);
        verify(inventoryRepository, times(1)).findByProductSku(anyString());
    }
}