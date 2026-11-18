package com.fashionshop.service;

import com.fashionshop.model.Product;
import java.util.List;
import java.util.Map;

/**
 * Service quản lý tích hợp Cơ sở dữ liệu Vector Qdrant
 * Hỗ trợ tạo Vector Embeddings, lập chỉ mục (indexing) và tìm kiếm ngữ nghĩa (Semantic Search).
 */
public interface QdrantService {

    /**
     * Kiểm tra trạng thái kết nối tới Qdrant Vector Server
     */
    boolean isQdrantConnected();

    /**
     * Khởi tạo Collection trong Qdrant nếu chưa có
     */
    boolean initCollection();

    /**
     * Đồng bộ danh sách sản phẩm từ MySQL sang Qdrant Vector Database
     */
    int syncProductsToQdrant(List<Product> products);

    /**
     * Tìm kiếm sản phẩm tương đồng ngữ nghĩa bằng Vector Cosine Similarity
     */
    List<Product> searchSimilarProducts(String query, int limit);

    /**
     * Lấy thông tin trạng thái hoạt động của module Qdrant
     */
    Map<String, Object> getQdrantStatus();
}
