package com.fashionshop.dao.impl;

import com.fashionshop.dao.CategoryDAO;
import com.fashionshop.model.Category;
import com.fashionshop.util.DBConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CategoryDAOImpl implements CategoryDAO {

    private final DBConnection dbConnection;

    public CategoryDAOImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY id";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getLong("id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                c.setImage(rs.getString("image"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Category findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getLong("id"));
                    c.setName(rs.getString("name"));
                    c.setDescription(rs.getString("description"));
                    c.setImage(rs.getString("image"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
