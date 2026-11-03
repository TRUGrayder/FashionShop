package com.fashionshop.service;

import com.fashionshop.model.Order;
import java.util.List;

public interface OrderService {
    Long placeOrder(Long userId, String shippingAddress, String phone, String note);
    List<Order> getOrdersByUser(Long userId);
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    boolean updateStatus(Long orderId, String status);
}
