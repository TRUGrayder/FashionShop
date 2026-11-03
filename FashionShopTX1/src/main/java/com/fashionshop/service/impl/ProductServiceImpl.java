package com.fashionshop.service.impl;

import com.fashionshop.dao.ProductDAO;
import com.fashionshop.model.Product;
import com.fashionshop.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;

    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productDAO.findById(id);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productDAO.findByCategory(categoryId);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDAO.search(keyword);
    }

    @Override
    public boolean createProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return false;
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            return false;
        }
        return productDAO.insert(product);
    }

    @Override
    public boolean updateProduct(Product product) {
        return productDAO.update(product);
    }

    @Override
    public boolean deleteProduct(Long id) {
        return productDAO.delete(id);
    }

    @Override
    public int countProducts() {
        return productDAO.count();
    }
}
