package com.orderengine.config;

import com.orderengine.entity.Inventory;
import com.orderengine.entity.Product;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public DatabaseInitializer(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            log.info("Seeding initial product catalog and inventory data...");

            Product gpu = Product.builder()
                    .name("NVIDIA RTX 5090 GPU (Limited Edition)")
                    .sku("GPU-RTX5090-FE")
                    .price(new BigDecimal("1999.99"))
                    .description("Ultra high-performance graphics card. High Contention Target!")
                    .build();
            gpu = productRepository.save(gpu);

            Product macbook = Product.builder()
                    .name("MacBook Pro 16\" M3 Max")
                    .sku("MBP-16-M3MAX")
                    .price(new BigDecimal("3499.00"))
                    .description("Pro laptop for creators and engineers")
                    .build();
            macbook = productRepository.save(macbook);

            Product ps5 = Product.builder()
                    .name("Sony PlayStation 5 Pro Console")
                    .sku("CONSOLE-PS5-PRO")
                    .price(new BigDecimal("699.99"))
                    .description("Next-gen gaming console")
                    .build();
            ps5 = productRepository.save(ps5);

            inventoryRepository.saveAll(List.of(
                    Inventory.builder().productId(gpu.getId()).availableQuantity(5).reservedQuantity(0).updatedAt(LocalDateTime.now()).build(),
                    Inventory.builder().productId(macbook.getId()).availableQuantity(500).reservedQuantity(0).updatedAt(LocalDateTime.now()).build(),
                    Inventory.builder().productId(ps5.getId()).availableQuantity(100).reservedQuantity(0).updatedAt(LocalDateTime.now()).build()
            ));

            log.info("Database seeding complete! Seeded 3 products.");
        }
    }
}
