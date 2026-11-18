package com.fashionshop;

import com.fashionshop.model.Product;
import com.fashionshop.service.QdrantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QdrantServiceTest {

    @Autowired
    private QdrantService qdrantService;

    private List<Product> sampleProducts;

    @BeforeEach
    void setUp() {
        sampleProducts = new ArrayList<>();

        Product p1 = new Product();
        p1.setId(101L);
        p1.setName("Áo thun Polo Classic Cotton");
        p1.setDescription("Chất vải cotton thoáng mát, phong cách dạo phố trẻ trung");
        p1.setCategoryName("Áo thun");
        p1.setPrice(BigDecimal.valueOf(250000));

        Product p2 = new Product();
        p2.setId(102L);
        p2.setName("Áo sơ mi Oxford công sở");
        p2.setDescription("Thanh lịch, lịch sự, phù hợp đi làm và phỏng vấn văn phòng");
        p2.setCategoryName("Sơ mi");
        p2.setPrice(BigDecimal.valueOf(380000));

        Product p3 = new Product();
        p3.setId(103L);
        p3.setName("Đầm body dự tiệc quý phái");
        p3.setDescription("Quyến rũ, sang trọng, dạ tiệc đám cưới tối");
        p3.setCategoryName("Đầm váy");
        p3.setPrice(BigDecimal.valueOf(650000));

        sampleProducts.add(p1);
        sampleProducts.add(p2);
        sampleProducts.add(p3);

        qdrantService.syncProductsToQdrant(sampleProducts);
    }

    @Test
    void testQdrantSyncAndStatus() {
        Map<String, Object> status = qdrantService.getQdrantStatus();
        assertNotNull(status);
        assertEquals(64, status.get("vectorDimension"), "Vector dimension phải là 64 chiều");
        assertTrue((int) status.get("indexedProductsCount") >= 3, "Số lượng vector lập chỉ mục phải >= 3");
        System.out.println(">>> Qdrant Status: " + status);
    }

    @Test
    void testSemanticVectorSearchWork() {
        // Khách tìm trang phục đi làm văn phòng
        List<Product> results = qdrantService.searchSimilarProducts("trang phục đi làm văn phòng phỏng vấn", 2);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Phải tìm thấy sản phẩm tương đồng vector");
        assertEquals("Áo sơ mi Oxford công sở", results.get(0).getName(), "Sản phẩm gần vector nhất phải là Sơ mi công sở");
        System.out.println(">>> Kết quả Semantic Search 'đi làm văn phòng': " + results.get(0).getName());
    }

    @Test
    void testSemanticVectorSearchParty() {
        // Khách tìm đồ đi đám cưới sang trọng
        List<Product> results = qdrantService.searchSimilarProducts("đồ đi đám cưới dạ tiệc sang trọng", 2);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("Đầm body dự tiệc quý phái", results.get(0).getName(), "Sản phẩm gần vector nhất phải là Đầm dự tiệc");
        System.out.println(">>> Kết quả Semantic Search 'dạ tiệc': " + results.get(0).getName());
    }
}
