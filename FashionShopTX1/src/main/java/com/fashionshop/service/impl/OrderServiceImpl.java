package com.fashionshop.service.impl;

import com.fashionshop.dao.CartDAO;
import com.fashionshop.dao.OrderDAO;
import com.fashionshop.dao.ProductDAO;
import com.fashionshop.model.*;
import com.fashionshop.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public OrderServiceImpl(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    public Long placeOrder(Long userId, String shippingAddress, String phone, String note) {
        List<CartItem> cartItems = cartDAO.findByUserId(userId);
        if (cartItems.isEmpty()) return null;

        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setNote(note);

        Long orderId = orderDAO.createOrder(order);
        if (orderId == null) return null;

        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setProductId(item.getProductId());
            detail.setProductName(item.getProduct().getName());
            detail.setPrice(item.getProduct().getPrice());
            detail.setQuantity(item.getQuantity());
            orderDAO.createOrderDetail(detail);

            // Trừ số lượng tồn kho của sản phẩm
            productDAO.reduceStock(item.getProductId(), item.getQuantity());
        }

        cartDAO.clearCart(userId);
        return orderId;
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderDAO.findByUserId(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderDAO.findById(id);
    }

    @Override
    public boolean updateStatus(Long orderId, String status) {
        if ("CANCELLED".equals(status)) {
            Order order = orderDAO.findById(orderId);
            if (order != null && !"CANCELLED".equals(order.getStatus()) && order.getDetails() != null) {
                // Hoàn lại số lượng tồn kho khi hủy đơn
                for (OrderDetail d : order.getDetails()) {
                    productDAO.reduceStock(d.getProductId(), -d.getQuantity());
                }
            }
        }
        return orderDAO.updateStatus(orderId, status);
    }
}
