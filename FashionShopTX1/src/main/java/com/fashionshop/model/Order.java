package com.fashionshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long id;
    private Long userId;
    private String userFullName;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String phone;
    private String note;
    private LocalDateTime createdAt;
    private List<OrderDetail> details;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderDetail> getDetails() { return details; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }

    public String getStatusText() {
        if (status == null) return "Chờ xử lý";
        switch (status) {
            case "PENDING": return "Chờ xử lý";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng";
            case "COMPLETED": return "Đã hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}
