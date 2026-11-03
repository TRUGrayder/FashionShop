package com.fashionshop.dao;

import com.fashionshop.model.Product;
import java.util.List;

/**
 * Interface DAO - đúng yêu cầu TX1
 */
public interface ProductDAO {
    List<Product> findAll();
    Product findById(Long id);
    List<Product> findByCategory(Long categoryId);
    List<Product> search(String keyword);
    boolean insert(Product product);
    boolean update(Product product);
    boolean delete(Long id);
    boolean reduceStock(Long productId, int quantity);
    int count();
}
