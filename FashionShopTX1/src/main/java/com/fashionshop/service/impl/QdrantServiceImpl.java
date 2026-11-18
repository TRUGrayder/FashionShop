package com.fashionshop.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fashionshop.model.Product;
import com.fashionshop.service.QdrantService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QdrantServiceImpl implements QdrantService {

    private static final Logger log = LoggerFactory.getLogger(QdrantServiceImpl.class);
    private static final int VECTOR_DIMENSION = 64;

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6333}")
    private int qdrantPort;

    @Value("${qdrant.collection-name:fashion_products}")
    private String collectionName;

    @Value("${qdrant.enabled:true}")
    private boolean qdrantEnabled;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Bộ nhớ đệm Vector Engine cục bộ (Local Vector Cache) phục vụ chạy Offline / Fallback
    private final Map<Long, VectorPoint> localVectorStore = new ConcurrentHashMap<>();

    private static class VectorPoint {
        Product product;
        float[] vector;

        VectorPoint(Product product, float[] vector) {
            this.product = product;
            this.vector = vector;
        }
    }

    public QdrantServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(1200))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private String getBaseUrl() {
        return "http://" + qdrantHost + ":" + qdrantPort;
    }

    @Override
    public boolean isQdrantConnected() {
        if (!qdrantEnabled) return false;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/readyz"))
                    .timeout(Duration.ofMillis(1200))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean initCollection() {
        if (!isQdrantConnected()) {
            log.info("[QDRANT] Server tại {}:{} chưa bật. Sẵn sàng chế độ Local Vector Engine dự phòng.", qdrantHost, qdrantPort);
            return false;
        }

        try {
            // Kiểm tra xem collection đã tồn tại chưa
            HttpRequest checkReq = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/collections/" + collectionName))
                    .timeout(Duration.ofMillis(1500))
                    .GET()
                    .build();
            HttpResponse<String> checkRes = httpClient.send(checkReq, HttpResponse.BodyHandlers.ofString());

            if (checkRes.statusCode() == 200) {
                log.info("[QDRANT] Collection '{}' đã tồn tại và sẵn sàng hoạt động.", collectionName);
                return true;
            }

            // Tạo collection mới với khoảng cách Cosine
            Map<String, Object> vectorsConfig = Map.of(
                    "size", VECTOR_DIMENSION,
                    "distance", "Cosine"
            );
            Map<String, Object> body = Map.of("vectors", vectorsConfig);
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/collections/" + collectionName))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(2000))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> createRes = httpClient.send(createReq, HttpResponse.BodyHandlers.ofString());
            boolean success = createRes.statusCode() == 200 || createRes.statusCode() == 201;
            if (success) {
                log.info("[QDRANT] Khởi tạo thành công Collection '{}' với Vector Dimension={}", collectionName, VECTOR_DIMENSION);
            }
            return success;
        } catch (Exception e) {
            log.warn("[QDRANT] Lỗi khởi tạo Collection: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int syncProductsToQdrant(List<Product> products) {
        if (products == null || products.isEmpty()) return 0;

        // 1. Luôn cập nhật vào Local Vector Store để đảm bảo luôn tìm kiếm được
        for (Product p : products) {
            float[] vector = generateVector(buildProductFeatureText(p));
            localVectorStore.put(p.getId(), new VectorPoint(p, vector));
        }

        log.info("[QDRANT-VECTOR-ENGINE] Đã số hóa vector thành công cho {} sản phẩm.", localVectorStore.size());

        // 2. Nếu Qdrant Server đang chạy, đồng bộ tiếp lên Qdrant Database
        if (isQdrantConnected()) {
            try {
                initCollection();

                List<Map<String, Object>> points = new ArrayList<>();
                for (Product p : products) {
                    float[] vector = localVectorStore.get(p.getId()).vector;
                    List<Float> vectorList = new ArrayList<>(vector.length);
                    for (float f : vector) vectorList.add(f);

                    Map<String, Object> payload = new HashMap<>();
                    payload.put("productId", p.getId());
                    payload.put("name", p.getName());
                    payload.put("price", p.getPrice() != null ? p.getPrice().doubleValue() : 0.0);
                    payload.put("description", p.getDescription() != null ? p.getDescription() : "");
                    payload.put("categoryName", p.getCategoryName() != null ? p.getCategoryName() : "");
                    payload.put("image", p.getImage() != null ? p.getImage() : "");

                    points.add(Map.of(
                            "id", p.getId(),
                            "vector", vectorList,
                            "payload", payload
                    ));
                }

                Map<String, Object> upsertBody = Map.of("points", points);
                String json = objectMapper.writeValueAsString(upsertBody);

                HttpRequest upsertReq = HttpRequest.newBuilder()
                        .uri(URI.create(getBaseUrl() + "/collections/" + collectionName + "/points?wait=true"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofMillis(3000))
                        .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(upsertReq, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    log.info("[QDRANT] Đã nạp thành công {} điểm vector lên Qdrant Server port {}.", points.size(), qdrantPort);
                }
            } catch (Exception e) {
                log.warn("[QDRANT] Không thể đẩy dữ liệu lên Qdrant: {}", e.getMessage());
            }
        }

        return localVectorStore.size();
    }

    @Override
    public List<Product> searchSimilarProducts(String query, int limit) {
        if (query == null || query.isBlank() || localVectorStore.isEmpty()) {
            return Collections.emptyList();
        }

        float[] queryVector = generateVector(query);

        // 1. Thử tìm kiếm qua Qdrant Server thật nếu server đang chạy
        if (isQdrantConnected()) {
            try {
                List<Float> qList = new ArrayList<>(queryVector.length);
                for (float f : queryVector) qList.add(f);

                Map<String, Object> searchBody = Map.of(
                        "vector", qList,
                        "limit", limit,
                        "with_payload", true
                );

                String json = objectMapper.writeValueAsString(searchBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(getBaseUrl() + "/collections/" + collectionName + "/points/search"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofMillis(2000))
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode resultNode = root.get("result");
                    if (resultNode != null && resultNode.isArray()) {
                        List<Product> results = new ArrayList<>();
                        for (JsonNode hit : resultNode) {
                            JsonNode payload = hit.get("payload");
                            if (payload != null) {
                                Product p = new Product();
                                p.setId(payload.get("productId").asLong());
                                p.setName(payload.get("name").asText());
                                p.setPrice(BigDecimal.valueOf(payload.get("price").asDouble()));
                                p.setDescription(payload.get("description").asText());
                                p.setCategoryName(payload.get("categoryName").asText());
                                p.setImage(payload.get("image").asText());
                                results.add(p);
                            }
                        }
                        if (!results.isEmpty()) {
                            log.info("[QDRANT-VECTOR-DB] Tìm thấy {} sản phẩm gần nhất qua Qdrant Server (Query: '{}')", results.size(), query);
                            return results;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("[QDRANT] Lỗi truy vấn Qdrant Server: {}, chuyển sang Local Vector Engine.", e.getMessage());
            }
        }

        // 2. Chế độ Local Vector Engine: Tính Cosine Similarity giữa Vector Query và Vector Sản phẩm
        List<Map.Entry<Product, Float>> scoredList = new ArrayList<>();
        for (VectorPoint point : localVectorStore.values()) {
            float similarity = computeCosineSimilarity(queryVector, point.vector);
            if (similarity > 0.05f) { // Lọc các sản phẩm có độ tương đồng dương
                scoredList.add(Map.entry(point.product, similarity));
            }
        }

        scoredList.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        List<Product> matched = scoredList.stream()
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("[QDRANT-LOCAL-VECTOR] Khớp {} sản phẩm qua thuật toán Cosine Similarity (Query: '{}')", matched.size(), query);
        return matched;
    }

    @Override
    public Map<String, Object> getQdrantStatus() {
        boolean connected = isQdrantConnected();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("connected", connected);
        status.put("mode", connected ? "Qdrant Vector Server Active" : "Local Vector Engine Fallback");
        status.put("host", qdrantHost);
        status.put("port", qdrantPort);
        status.put("collection", collectionName);
        status.put("vectorDimension", VECTOR_DIMENSION);
        status.put("indexedProductsCount", localVectorStore.size());
        return status;
    }

    // =========================================================================
    // THUẬT TOÁN VECTOR EMBEDDING NGUYÊN BẢN (TEXT TO VECTOR)
    // =========================================================================

    private String buildProductFeatureText(Product p) {
        StringBuilder sb = new StringBuilder();
        if (p.getName() != null) sb.append(p.getName()).append(" ");
        if (p.getDescription() != null) sb.append(p.getDescription()).append(" ");
        if (p.getCategoryName() != null) sb.append(p.getCategoryName()).append(" ");

        // Gắn thêm các từ khóa phong cách, ngữ cảnh tự nhiên
        String text = sb.toString().toLowerCase();
        if (text.contains("áo thun") || text.contains("polo") || text.contains("cotton")) {
            sb.append(" năng động dạo phố mùa hè mát mẻ trẻ trung thường ngày basic");
        }
        if (text.contains("sơ mi") || text.contains("blazer") || text.contains("công sở")) {
            sb.append(" lịch lãm chuyên nghiệp phỏng vấn đi làm trang trọng văn phòng");
        }
        if (text.contains("jean") || text.contains("baggy") || text.contains("slim")) {
            sb.append(" cá tính bền bỉ hiện đại streetwear đi chơi");
        }
        if (text.contains("đầm") || text.contains("váy") || text.contains("body") || text.contains("dự tiệc")) {
            sb.append(" quyến rũ sang trọng tiệc tùng đám cưới quý phái nổi bật");
        }
        if (text.contains("khoác") || text.contains("bomber")) {
            sb.append(" ấm áp mùa đông thu đông phong cách ngầu giữ nhiệt");
        }
        return sb.toString();
    }

    /**
     * Tạo vector 64 chiều chuẩn hóa L2 từ chuỗi văn bản
     */
    private float[] generateVector(String text) {
        float[] vector = new float[VECTOR_DIMENSION];
        if (text == null || text.isBlank()) return vector;

        String normalized = removeAccents(text.toLowerCase());
        String[] words = normalized.split("\\s+");

        for (String word : words) {
            if (word.length() < 2) continue;

            // Băm từ vựng vào các chiều vector
            int hash1 = Math.abs(word.hashCode()) % VECTOR_DIMENSION;
            int hash2 = Math.abs((word + "_sub").hashCode()) % VECTOR_DIMENSION;

            vector[hash1] += 1.2f;
            vector[hash2] += 0.8f;
        }

        // Chuẩn hóa vector theo độ dài Euclidean (L2-norm) để khoảng cách Cosine = Dot Product
        float norm = 0f;
        for (float v : vector) norm += v * v;
        norm = (float) Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    /**
     * Tính độ tương đồng Cosine giữa 2 vector đã chuẩn hóa L2
     */
    private float computeCosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) return 0f;
        float dotProduct = 0f;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
        }
        return dotProduct;
    }

    private String removeAccents(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
}
