package com.orderengine.service;

import com.orderengine.dto.InventoryDTO;
import com.orderengine.entity.ContentionLevel;
import com.orderengine.entity.Inventory;
import com.orderengine.entity.Product;
import com.orderengine.inventory.AdaptiveInventoryGuardService;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final AdaptiveInventoryGuardService adaptiveGuard;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            AdaptiveInventoryGuardService adaptiveGuard) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.adaptiveGuard = adaptiveGuard;
    }

    @Transactional(readOnly = true)
    public List<InventoryDTO> getAllInventoryStatus() {
        List<Inventory> inventories = inventoryRepository.findAll();
        return inventories.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory for product #" + productId + " not found"));
        return toDTO(inventory);
    }

    @Transactional
    public Inventory setInventoryStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> Inventory.builder()
                        .productId(productId)
                        .availableQuantity(quantity)
                        .reservedQuantity(0)
                        .updatedAt(LocalDateTime.now())
                        .build());
        inventory.setAvailableQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    private InventoryDTO toDTO(Inventory inventory) {
        Long pid = inventory.getProductId();
        Product product = productRepository.findById(pid).orElse(null);
        String name = product != null ? product.getName() : "Unknown Product";
        String sku = product != null ? product.getSku() : "SKU-" + pid;

        ContentionLevel level = adaptiveGuard.evaluateContentionLevel(pid);
        double reqSec = adaptiveGuard.getRequestsPerSecond(pid);
        long successCount = adaptiveGuard.getSuccessfulAllocations(pid);
        long failCount = adaptiveGuard.getFailedAllocations(pid);

        return InventoryDTO.builder()
                .productId(pid)
                .productName(name)
                .sku(sku)
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .contentionLevel(level)
                .requestsPerSecond(reqSec)
                .successfulAllocations(successCount)
                .failedAllocations(failCount)
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
