package com.fashionshop.dao.impl;

import com.fashionshop.dao.OrderDAO;
import com.fashionshop.model.Order;
import com.fashionshop.model.OrderDetail;
import com.fashionshop.util.DBConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderDAOImpl implements OrderDAO {

    private final DBConnection dbConnection;

    public OrderDAOImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Long createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getUserId());
            ps.setBigDecimal(2, order.getTotalAmount());
            ps.setString(3, order.getStatus() != null ? order.getStatus() : "PENDING");
            ps.setString(4, order.getShippingAddress());
            ps.setString(5, order.getPhone());
            ps.setString(6, order.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean createOrderDetail(OrderDetail detail) {
        String sql = "INSERT INTO order_details (order_id, product_id, product_name, price, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, detail.getOrderId());
            ps.setLong(2, detail.getProductId());
            ps.setString(3, detail.getProductName());
            ps.setBigDecimal(4, detail.getPrice());
            ps.setInt(5, detail.getQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name FROM orders o JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setUserFullName(rs.getString("full_name"));
                list.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Order findById(Long id) {
        String sql = "SELECT o.*, u.full_name FROM orders o JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setUserFullName(rs.getString("full_name"));
                    order.setDetails(findDetailsByOrderId(id));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateStatus(Long orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<OrderDetail> findDetailsByOrderId(Long orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM order_details WHERE order_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail d = new OrderDetail();
                    d.setId(rs.getLong("id"));
                    d.setOrderId(rs.getLong("order_id"));
                    d.setProductId(rs.getLong("product_id"));
                    d.setProductName(rs.getString("product_name"));
                    d.setPrice(rs.getBigDecimal("price"));
                    d.setQuantity(rs.getInt("quantity"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setUserId(rs.getLong("user_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setShippingAddress(rs.getString("shipping_address"));
        o.setPhone(rs.getString("phone"));
        o.setNote(rs.getString("note"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
        return o;
    }
}
