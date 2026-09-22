package com.orderengine.controller;

import com.orderengine.dto.ProductDTO;
import com.orderengine.entity.Inventory;
import com.orderengine.entity.Product;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductController(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productRepository.findAll().stream()
                .map(ProductDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product #" + id + " not found"));
        return ResponseEntity.ok(ProductDTO.fromEntity(product));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();
        Product saved = productRepository.save(product);

        Inventory inventory = Inventory.builder()
                .productId(saved.getId())
                .availableQuantity(request.getInitialStock() != null ? request.getInitialStock() : 0)
                .reservedQuantity(0)
                .updatedAt(LocalDateTime.now())
                .build();
        inventoryRepository.save(inventory);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProductDTO.fromEntity(saved));
    }

    public static class CreateProductRequest {
        private String name;
        private String sku;
        private BigDecimal price;
        private String description;
        private Integer initialStock;

        public CreateProductRequest() {}

        public CreateProductRequest(String name, String sku, BigDecimal price, String description, Integer initialStock) {
            this.name = name;
            this.sku = sku;
            this.price = price;
            this.description = description;
            this.initialStock = initialStock;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getInitialStock() { return initialStock; }
        public void setInitialStock(Integer initialStock) { this.initialStock = initialStock; }
    }
}
