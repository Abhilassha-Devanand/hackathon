package com.orderengine.controller;

import com.orderengine.dto.InventoryDTO;
import com.orderengine.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventoryStatus());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @PostMapping("/{productId}/stock")
    public ResponseEntity<InventoryDTO> updateStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        inventoryService.setInventoryStock(productId, quantity);
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }
}
