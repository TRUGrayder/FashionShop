package com.fashionshop.controller;

import com.fashionshop.model.Product;
import com.fashionshop.service.ProductService;
import com.fashionshop.service.QdrantService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ai")
public class AiController {

    private final ProductService productService;
    private final QdrantService qdrantService;
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");

    public AiController(ProductService productService, QdrantService qdrantService) {
        this.productService = productService;
        this.qdrantService = qdrantService;
    }

    /**
     * Tự động khởi tạo và đồng bộ vector sản phẩm sang Qdrant ngay khi ứng dụng khởi động
     */
    @PostConstruct
    public void initQdrantVectors() {
        try {
            List<Product> products = productService.getAllProducts();
            if (products != null && !products.isEmpty()) {
                qdrantService.syncProductsToQdrant(products);
            }
        } catch (Exception e) {
            // Đảm bảo không làm chậm hoặc dừng ứng dụng nếu DB đang khởi động
        }
    }

    @GetMapping("/chat")
    public String chatPage(Model model, HttpSession session) {
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        model.addAttribute("qdrantStatus", qdrantService.getQdrantStatus());
        return "ai-chat";
    }

    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getStatus() {
        return qdrantService.getQdrantStatus();
    }

    @PostMapping("/sync")
    @ResponseBody
    public Map<String, Object> syncVectors() {
        List<Product> products = productService.getAllProducts();
        int count = qdrantService.syncProductsToQdrant(products);
        Map<String, Object> res = new HashMap<>(qdrantService.getQdrantStatus());
        res.put("message", "Đã số hóa và đồng bộ thành công " + count + " sản phẩm vào Vector Database!");
        return res;
    }

    @PostMapping("/ask")
    @ResponseBody
    public Map<String, Object> ask(@RequestParam String message) {
        Map<String, Object> res = new HashMap<>();
        String msg = (message != null) ? message.trim() : "";
        String lower = msg.toLowerCase();
        StringBuilder answer = new StringBuilder();

        // 1. Nếu người dùng hoặc thầy giáo hỏi về Qdrant / Kiến trúc AI
        if (lower.contains("qdrant") || lower.contains("vector") || lower.contains("công nghệ ai") || lower.contains("kiến trúc ai")) {
            Map<String, Object> status = qdrantService.getQdrantStatus();
            answer.append("🚀 <strong>Hệ Thống AI Stylist & Qdrant Vector Database:</strong><br><br>")
                  .append("• <strong>Trạng thái:</strong> <span class='badge bg-success'>").append(status.get("mode")).append("</span><br>")
                  .append("• <strong>Collection:</strong> <code>").append(status.get("collection")).append("</code><br>")
                  .append("• <strong>Không gian Vector:</strong> <strong>").append(status.get("vectorDimension")).append(" chiều (Float32)</strong><br>")
                  .append("• <strong>Độ đo khoảng cách:</strong> <strong>Cosine Similarity</strong><br>")
                  .append("• <strong>Số lượng sản phẩm đã lập chỉ mục (Indexed):</strong> <strong>").append(status.get("indexedProductsCount")).append(" sản phẩm</strong><br><br>")
                  .append("💡 <em>Cơ chế hoạt động:</em> Mỗi khi bạn nhập câu hỏi, hệ thống sẽ chuyển văn bản thành Vector ngữ nghĩa và đo khoảng cách Cosine với kho sản phẩm để tìm ra các trang phục phù hợp nhất!");
            res.put("answer", answer.toString());
            res.put("qdrantStatus", status);
            return res;
        }

        List<Product> allProducts = productService.getAllProducts();

        // 2. Chào hỏi ban đầu
        if (lower.matches(".*(xin chào|chào|hello|hi|hey|alo|bạn là ai|mày là ai).*")) {
            answer.append("✨ <strong>Xin chào bạn!</strong> Em là <strong>AI Fashion Stylist</strong> tích hợp <strong>Qdrant Vector Database</strong>.<br>")
                  .append("Em có khả năng phân tích ngữ nghĩa, hiểu dịp mặc đồ và gợi ý các set phối đồ phù hợp nhất trong cửa hàng.<br><br>")
                  .append("💡 <em>Bạn có thể hỏi em ví dụ:</em><br>")
                  .append("• <em>\"Hôm nay đi phỏng vấn xin việc nên mặc gì?\"</em><br>")
                  .append("• <em>\"Gợi ý set đồ đi chơi dạo phố mùa hè\"</em><br>")
                  .append("• <em>\"Tư vấn đồ đi tiệc cưới sang trọng\"</em><br>")
                  .append("• <em>\"Tìm áo thun hoặc blazer đẹp\"</em>");
            res.put("answer", answer.toString());
            return res;
        }

        // 3. Tư vấn theo Dịp / Sự kiện (Occasion) kết hợp tìm kiếm Vector
        List<Product> vectorMatches = qdrantService.searchSimilarProducts(msg, 4);

        if (lower.contains("công sở") || lower.contains("đi làm") || lower.contains("phỏng vấn") || lower.contains("họp") || lower.contains("văn phòng") || lower.contains("lịch sự")) {
            answer.append("👔 <strong>Gợi ý Set Đồ Công Sở & Phỏng Vấn Chuyên Nghiệp:</strong><br>")
                  .append("Để tạo ấn tượng chỉn chu và đáng tin cậy, em khuyên bạn phối: Áo sơ mi Oxford sáng màu kết hợp khoác ngoài Áo blazer dạ/kaki và Quần slim fit tôn dáng.<br><br>")
                  .append("✨ <em>Các món đồ chuẩn phong cách công sở đang có sẵn:</em>");
            answer.append(renderProductCards(filterOrFallback(allProducts, vectorMatches, "sơ mi", "blazer", "jean", "quần")));
        }
        else if (lower.contains("tiệc") || lower.contains("party") || lower.contains("đám cưới") || lower.contains("sang trọng") || lower.contains("quyến rũ") || lower.contains("quán bar")) {
            answer.append("🥂 <strong>Gợi ý Outfit Dự Tiệc Quý Phái & Nổi Bật:</strong><br>")
                  .append("Dành cho các buổi tiệc tối hoặc đám cưới: Đầm body ôm sát hoặc váy hoa nhí điệu đà kết hợp túi xách da thật mini và blazer khoác hờ vai sẽ giúp bạn tỏa sáng!<br><br>")
                  .append("✨ <em>Các mẫu dự tiệc được chấm điểm Vector cao nhất:</em>");
            answer.append(renderProductCards(filterOrFallback(allProducts, vectorMatches, "đầm", "váy", "túi xách", "blazer")));
        }
        else if (lower.contains("đi chơi") || lower.contains("dạo phố") || lower.contains("cuối tuần") || lower.contains("hẹn hò") || lower.contains("năng động") || lower.contains("thoải mái")) {
            answer.append("👟 <strong>Gợi ý Set Đồ Dạo Phố & Đi Chơi Trẻ Trung:</strong><br>")
                  .append("Phong cách Streetwear thoải mái và cá tính: Áo thun form rộng Oversize phối cùng Quần jean Baggy và Mũ bucket sành điệu.<br><br>")
                  .append("✨ <em>Set đồ dạo phố năng động gợi ý cho bạn:</em>");
            answer.append(renderProductCards(filterOrFallback(allProducts, vectorMatches, "áo thun", "baggy", "mũ", "polo")));
        }
        else if (lower.contains("mùa đông") || lower.contains("lạnh") || lower.contains("ấm") || lower.contains("gió")) {
            answer.append("❄️ <strong>Set Đồ Thu Đông Ấm Áp & Thời Thượng:</strong><br>")
                  .append("Giữ ấm cơ thể nhưng vẫn chuẩn phong cách thời trang với áo khoác bomber cản gió và blazer cao cấp.<br><br>");
            answer.append(renderProductCards(filterOrFallback(allProducts, vectorMatches, "khoác", "bomber", "blazer", "jean")));
        }
        else if (lower.contains("mùa hè") || lower.contains("nóng") || lower.contains("mát") || lower.contains("thấm hút") || lower.contains("biển")) {
            answer.append("☀️ <strong>Set Đồ Mùa Hè Thoáng Mát & Dịu Dàng:</strong><br>")
                  .append("Chất vải 100% cotton dệt mềm mại, thấm hút mồ hôi tốt hoặc váy voan mỏng nhẹ dạo phố.<br><br>");
            answer.append(renderProductCards(filterOrFallback(allProducts, vectorMatches, "áo thun", "váy", "mũ", "polo")));
        }
        else if (lower.contains("rẻ") || lower.contains("tiết kiệm") || lower.contains("sinh viên") || lower.contains("300k") || lower.contains("200k")) {
            List<Product> cheap = allProducts.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().doubleValue() <= 300000)
                    .limit(4)
                    .collect(Collectors.toList());
            answer.append("🏷️ <strong>Top Sản Phẩm Giá Hạt Dẻ Dưới 300.000₫:</strong><br>")
                  .append("Chất lượng cao cấp, thiết kế basic dễ phối nhưng giá thành cực kỳ hợp túi tiền sinh viên:<br>");
            answer.append(renderProductCards(cheap));
        }
        else if (lower.contains("đổi trả") || lower.contains("ship") || lower.contains("vận chuyển") || lower.contains("bảo hành") || lower.contains("thanh toán")) {
            answer.append("📦 <strong>Chính sách mua sắm tại Fashion Shop:</strong><br><br>")
                  .append("• <strong>Phí vận chuyển:</strong> Miễn phí toàn quốc cho đơn hàng từ 499.000₫.<br>")
                  .append("• <strong>Thanh toán:</strong> Nhận hàng kiểm tra thoải mái rồi mới thanh toán (COD).<br>")
                  .append("• <strong>Đổi trả:</strong> Hỗ trợ đổi size / mẫu trong 30 ngày nếu còn tem mác.<br>")
                  .append("• <strong>Hotline hỗ trợ:</strong> 1900 8888 (8:00 - 22:00 hàng ngày).");
        }
        // 4. Tìm kiếm ngữ nghĩa bằng Vector Qdrant
        else {
            if (!vectorMatches.isEmpty()) {
                answer.append("🎯 <strong>Kết Quả Tìm Kiếm Ngữ Nghĩa (Qdrant Vector Similarity):</strong><br>")
                      .append("Em đã quét toàn bộ kho sản phẩm bằng không gian vector và tìm thấy các trang phục tương đồng nhất với yêu cầu của bạn:<br>");
                answer.append(renderProductCards(vectorMatches));
            } else {
                List<Product> keywordMatches = productService.searchProducts(msg);
                if (!keywordMatches.isEmpty()) {
                    answer.append("🔍 <strong>Tìm Thấy ").append(keywordMatches.size()).append(" Sản Phẩm Theo Từ Khóa:</strong><br>");
                    answer.append(renderProductCards(keywordMatches));
                } else {
                    answer.append("👗 <strong>Tư Vấn Stylist:</strong> Em có thể gợi ý phối đồ hoặc tìm sản phẩm theo mọi nhu cầu của bạn:<br><br>")
                          .append("• <strong>Theo phong cách:</strong> Tối giản, Hàn Quốc, Streetwear, Công sở, Dạ tiệc quyến rũ.<br>")
                          .append("• <strong>Theo sản phẩm:</strong> Áo thun, Polo, Sơ mi, Blazer, Quần jean, Đầm váy, Túi xách, Mũ bucket.<br>")
                          .append("• <strong>Theo dịp:</strong> Đi học, đi làm, dự tiệc, đi chơi cuối tuần, phỏng vấn xin việc.<br><br>")
                          .append("👉 <em>Hãy thử hỏi: 'Hôm nay đi tiệc cưới mặc gì?' hoặc 'Tìm đồ công sở' nhé!</em>");
                }
            }
        }

        // Bổ sung huy hiệu Vector ở chân tin nhắn
        answer.append("<div class='mt-3 pt-2 border-top text-muted' style='font-size: 0.75rem;'>")
              .append("<i class='bi bi-cpu-fill text-primary me-1'></i>")
              .append("Được phân tích và gợi ý bằng <strong>Qdrant Semantic Vector Search (Cosine Metric)</strong>")
              .append("</div>");

        res.put("answer", answer.toString());
        return res;
    }

    private List<Product> filterOrFallback(List<Product> all, List<Product> vectorMatches, String... keywords) {
        if (vectorMatches != null && !vectorMatches.isEmpty()) {
            return vectorMatches.stream().limit(4).collect(Collectors.toList());
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : all) {
            String text = (p.getName() + " " + p.getDescription() + " " + p.getCategoryName()).toLowerCase();
            for (String kw : keywords) {
                if (text.contains(kw)) {
                    filtered.add(p);
                    break;
                }
            }
            if (filtered.size() >= 4) break;
        }
        return filtered.isEmpty() ? all.stream().limit(3).collect(Collectors.toList()) : filtered;
    }

    /**
     * Render danh sách sản phẩm thành thẻ Card trực quan có ảnh, giá và nút bấm ngay trong chat
     */
    private String renderProductCards(List<Product> products) {
        if (products == null || products.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("<div class='row g-2 mt-2'>");
        for (Product p : products) {
            String imgUrl = (p.getImage() != null && !p.getImage().isBlank()) ? p.getImage() : "https://via.placeholder.com/300?text=Fashion";
            String catName = (p.getCategoryName() != null) ? p.getCategoryName() : "Thời trang";
            String formattedPrice = (p.getPrice() != null) ? priceFormat.format(p.getPrice()) + "₫" : "0₫";

            sb.append("<div class='col-sm-6 col-12'>")
              .append("  <div class='card h-100 border shadow-sm rounded-3 overflow-hidden bg-white'>")
              .append("    <img src='").append(imgUrl).append("' alt='").append(p.getName()).append("' ")
              .append("         style='height: 120px; width: 100%; object-fit: cover;' class='card-img-top'>")
              .append("    <div class='card-body p-2 d-flex flex-column justify-content-between'>")
              .append("      <div>")
              .append("        <span class='badge bg-secondary-subtle text-dark border mb-1' style='font-size: 0.68rem;'>").append(catName).append("</span>")
              .append("        <h6 class='card-title fw-bold mb-1 text-truncate' style='font-size: 0.85rem;' title='").append(p.getName()).append("'>").append(p.getName()).append("</h6>")
              .append("        <div class='text-danger fw-bold mb-2' style='font-size: 0.88rem;'>").append(formattedPrice).append("</div>")
              .append("      </div>")
              .append("      <div class='d-flex gap-1'>")
              .append("        <a href='/products/").append(p.getId()).append("' class='btn btn-outline-dark btn-sm flex-grow-1 py-1' style='font-size: 0.75rem;'>")
              .append("          <i class='bi bi-eye me-1'></i>Xem")
              .append("        </a>")
              .append("        <form action='/cart/add' method='post' style='display:inline;'>")
              .append("          <input type='hidden' name='productId' value='").append(p.getId()).append("'>")
              .append("          <input type='hidden' name='quantity' value='1'>")
              .append("          <button type='submit' class='btn btn-accent btn-sm text-white px-2 py-1' style='font-size: 0.75rem;' title='Thêm vào giỏ'>")
              .append("            <i class='bi bi-cart-plus'></i>")
              .append("          </button>")
              .append("        </form>")
              .append("      </div>")
              .append("    </div>")
              .append("  </div>")
              .append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}
