package com.fashionshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String image;
    private Long categoryId;
    private String categoryName;   // join
    private Integer status;
    private LocalDateTime createdAt;

    public Product() {}

    public Product(Long id, String name, String description, BigDecimal price, Integer quantity, String image, Long categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
        this.categoryId = categoryId;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
