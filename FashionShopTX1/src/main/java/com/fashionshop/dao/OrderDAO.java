package com.fashionshop.dao;

import com.fashionshop.model.Order;
import com.fashionshop.model.OrderDetail;
import java.util.List;

public interface OrderDAO {
    Long createOrder(Order order);
    boolean createOrderDetail(OrderDetail detail);
    List<Order> findByUserId(Long userId);
    List<Order> findAll();
    Order findById(Long id);
    boolean updateStatus(Long orderId, String status);
    List<OrderDetail> findDetailsByOrderId(Long orderId);
}
