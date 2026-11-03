package com.fashionshop.dao.impl;

import com.fashionshop.dao.CartDAO;
import com.fashionshop.model.CartItem;
import com.fashionshop.model.Product;
import com.fashionshop.util.DBConnection;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CartDAOImpl implements CartDAO {

    private final DBConnection dbConnection;

    public CartDAOImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT ci.*, p.name, p.price, p.image, p.quantity AS stock " +
                     "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                     "WHERE ci.user_id = ? ORDER BY ci.id DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));

                    Product p = new Product();
                    p.setId(rs.getLong("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setImage(rs.getString("image"));
                    p.setQuantity(rs.getInt("stock"));
                    item.setProduct(p);

                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public CartItem findByUserAndProduct(Long userId, Long productId) {
        String sql = "SELECT * FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean addItem(Long userId, Long productId, int quantity) {
        CartItem existing = findByUserAndProduct(userId, productId);
        if (existing != null) {
            return updateQuantity(userId, productId, existing.getQuantity() + quantity);
        }
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateQuantity(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeItem(userId, productId);
        }
        String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, userId);
            ps.setLong(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeItem(Long userId, Long productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean clearCart(Long userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int countItems(Long userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
