package com.fashionshop.dao.impl;

import com.fashionshop.dao.ProductDAO;
import com.fashionshop.model.Product;
import com.fashionshop.util.DBConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductDAOImpl implements ProductDAO {

    private final DBConnection dbConnection;

    public ProductDAOImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.id DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Product findById(Long id) {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.category_id = ? AND p.status = 1 ORDER BY p.id DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Product> search(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE (p.name LIKE ? OR p.description LIKE ? OR c.name LIKE ?) " +
                     "AND p.status = 1 ORDER BY p.id DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(Product product) {
        String sql = "INSERT INTO products (name, description, price, quantity, image, category_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity() != null ? product.getQuantity() : 0);
            ps.setString(5, product.getImage());
            ps.setObject(6, product.getCategoryId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET name=?, description=?, price=?, quantity=?, image=?, category_id=? WHERE id=?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity() != null ? product.getQuantity() : 0);
            ps.setString(5, product.getImage());
            ps.setObject(6, product.getCategoryId());
            ps.setLong(7, product.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Nếu bị vướng ràng buộc khóa ngoại với đơn hàng cũ, tự động chuyển sang Soft Delete (ẩn sản phẩm)
            String softDeleteSql = "UPDATE products SET status = 0 WHERE id = ?";
            try (Connection conn2 = dbConnection.getConnection();
                 PreparedStatement ps2 = conn2.prepareStatement(softDeleteSql)) {
                ps2.setLong(1, id);
                return ps2.executeUpdate() > 0;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean reduceStock(Long productId, int quantity) {
        String sql = "UPDATE products SET quantity = GREATEST(0, quantity - ?) WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setImage(rs.getString("image"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setStatus(rs.getInt("status"));
        return p;
    }
}
