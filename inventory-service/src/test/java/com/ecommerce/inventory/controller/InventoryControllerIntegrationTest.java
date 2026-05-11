package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    private InventoryResponse testResponse;
    private InventoryRequest testRequest;

    @BeforeEach
    void setUp() {
        testResponse = new InventoryResponse(1L, "TEST-SKU-001", 100, 10, 90);

        testRequest = new InventoryRequest();
        testRequest.setProductSku("TEST-SKU-001");
        testRequest.setQuantity(100);
    }

    @Test
    void testCreateInventory_Success() throws Exception {
        // Arrange
        when(inventoryService.createInventory(any(InventoryRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productSku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(inventoryService, times(1)).createInventory(any(InventoryRequest.class));
    }

    @Test
    void testGetAllInventory_Success() throws Exception {
        // Arrange
        InventoryResponse response2 = new InventoryResponse(2L, "TEST-SKU-002", 200, 20, 180);
        List<InventoryResponse> responses = Arrays.asList(testResponse, response2);
        when(inventoryService.getAllInventory()).thenReturn(responses);

        // Act & Assert
        mockMvc.perform(get("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productSku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$[1].productSku").value("TEST-SKU-002"));

        verify(inventoryService, times(1)).getAllInventory();
    }

    @Test
    void testGetInventoryBySku_Success() throws Exception {
        // Arrange
        when(inventoryService.getInventoryBySku(anyString())).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/TEST-SKU-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.availableQuantity").value(90));

        verify(inventoryService, times(1)).getInventoryBySku("TEST-SKU-001");
    }

    @Test
    void testCheckStock_InStock() throws Exception {
        // Arrange
        when(inventoryService.isInStock(anyString(), anyInt())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/TEST-SKU-001/check")
                        .param("quantity", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(inventoryService, times(1)).isInStock("TEST-SKU-001", 50);
    }

    @Test
    void testCheckStock_OutOfStock() throws Exception {
        // Arrange
        when(inventoryService.isInStock(anyString(), anyInt())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/TEST-SKU-001/check")
                        .param("quantity", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(inventoryService, times(1)).isInStock("TEST-SKU-001", 200);
    }

    @Test
    void testUpdateStock_Success() throws Exception {
        // Arrange
        InventoryResponse updatedResponse = new InventoryResponse(1L, "TEST-SKU-001", 150, 10, 140);
        when(inventoryService.updateStock(anyString(), anyInt())).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/inventory/TEST-SKU-001/stock")
                        .param("quantity", "150")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(150))
                .andExpect(jsonPath("$.availableQuantity").value(140));

        verify(inventoryService, times(1)).updateStock("TEST-SKU-001", 150);
    }

    @Test
    void testReserveStock_Success() throws Exception {
        // Arrange
        doNothing().when(inventoryService).reserveStock(anyString(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/inventory/TEST-SKU-001/reserve")
                        .param("quantity", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(inventoryService, times(1)).reserveStock("TEST-SKU-001", 20);
    }

    @Test
    void testReleaseStock_Success() throws Exception {
        // Arrange
        doNothing().when(inventoryService).releaseStock(anyString(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/inventory/TEST-SKU-001/release")
                        .param("quantity", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(inventoryService, times(1)).releaseStock("TEST-SKU-001", 10);
    }
}