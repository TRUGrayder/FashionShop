package com.fashionshop.service;

import com.fashionshop.model.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> getProductsByCategory(Long categoryId);
    List<Product> searchProducts(String keyword);
    boolean createProduct(Product product);
    boolean updateProduct(Product product);
    boolean deleteProduct(Long id);
    int countProducts();
}
