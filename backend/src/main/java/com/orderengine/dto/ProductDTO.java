package com.orderengine.dto;

import com.orderengine.entity.Product;
import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private String description;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String sku, BigDecimal price, String description) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getDescription()
        );
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal price;
        private String description;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder sku(String sku) { this.sku = sku; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder description(String description) { this.description = description; return this; }

        public ProductDTO build() {
            return new ProductDTO(id, name, sku, price, description);
        }
    }
}
