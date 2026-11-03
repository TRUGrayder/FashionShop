package com.fashionshop;

import com.fashionshop.model.Product;
import com.fashionshop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case minh họa cho TX1 (≥ 2 test case)
 */
@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void testGetAllProducts() {
        List<Product> products = productService.getAllProducts();
        assertNotNull(products);
        // Có thể assertTrue(products.size() > 0) nếu đã có data
        System.out.println("Test 1 - Số sản phẩm: " + products.size());
    }

    @Test
    void testCreateProductValidation() {
        Product invalid = new Product();
        invalid.setName("");          // tên rỗng → phải fail
        invalid.setPrice(BigDecimal.ZERO);

        boolean result = productService.createProduct(invalid);
        assertFalse(result);
        System.out.println("Test 2 - Validation tên rỗng: " + (result ? "FAIL" : "PASS"));
    }
}
