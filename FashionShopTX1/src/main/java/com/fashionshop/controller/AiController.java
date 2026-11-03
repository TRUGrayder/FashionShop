package com.fashionshop.controller;

import com.fashionshop.model.Product;
import com.fashionshop.service.ProductService;
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
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");

    public AiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/chat")
    public String chatPage(Model model, HttpSession session) {
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "ai-chat";
    }

    @PostMapping("/ask")
    @ResponseBody
    public Map<String, String> ask(@RequestParam String message) {
        Map<String, String> res = new HashMap<>();
        String msg = (message != null) ? message.trim() : "";
        String lower = msg.toLowerCase();
        String answer;

        List<Product> allProducts = productService.getAllProducts();

        // 1. Chào hỏi
        if (lower.matches(".*(xin chào|chào|hello|hi|hey|alo|bạn là ai|mày là ai).*")) {
            answer = "✨ <strong>Xin chào bạn!</strong> Em là <strong>AI Fashion Stylist</strong> của Fashion Shop.<br>"
                    + "Em được huấn luyện để tư vấn phối đồ (Mix & Match), gợi ý trang phục theo từng dịp, sự kiện hoặc phong cách bạn yêu thích.<br><br>"
                    + "💡 <em>Bạn có thể hỏi em ví dụ:</em><br>"
                    + "• <em>\"Hôm nay đi phỏng vấn xin việc nên mặc gì?\"</em><br>"
                    + "• <em>\"Gợi ý set đồ đi chơi dạo phố mùa hè\"</em><br>"
                    + "• <em>\"Tư vấn đồ đi tiệc tối sang trọng\"</em><br>"
                    + "• <em>\"Có áo thun hoặc quần jean nào đẹp không?\"</em>";
        }
        // 2. Đi làm / Phỏng vấn / Công sở
        else if (lower.contains("công sở") || lower.contains("đi làm") || lower.contains("phỏng vấn") || lower.contains("họp") || lower.contains("văn phòng") || lower.contains("lịch sự")) {
            answer = "👔 <strong>Gợi ý Set đồ Công sở & Đi phỏng vấn lịch lãm:</strong><br><br>"
                    + "Để tạo ấn tượng chuyên nghiệp, chỉn chu và thanh lịch, em gợi ý bạn công thức phối:<br>"
                    + "1️⃣ <a href='/products/4' class='text-danger fw-bold text-decoration-none'>Áo sơ mi Oxford trắng</a> (399.000₫) hoặc <a href='/products/5' class='text-danger fw-bold text-decoration-none'>Áo sơ mi kẻ sọc</a> (379.000₫) chất vải chống nhăn.<br>"
                    + "2️⃣ Khoác ngoài cùng <a href='/products/11' class='text-danger fw-bold text-decoration-none'>Áo blazer công sở</a> (749.000₫) form dáng chuẩn sang trọng.<br>"
                    + "3️⃣ Kết hợp cùng <a href='/products/6' class='text-danger fw-bold text-decoration-none'>Quần jean Slim Fit</a> (499.000₫) màu tối và giày da/sneaker trắng tối giản.<br><br>"
                    + "👉 <em>Tip nhỏ:</em> Sơ vin áo sơ mi và cài 1 nút blazer sẽ giúp tôn dáng và tạo phong thái tự tin!";
        }
        // 3. Đi tiệc / Dự tiệc / Party / Đám cưới
        else if (lower.contains("tiệc") || lower.contains("party") || lower.contains("đám cưới") || lower.contains("sang trọng") || lower.contains("quyến rũ") || lower.contains("quán bar")) {
            answer = "🥂 <strong>Gợi ý Outfit Dự Tiệc Quý Phái & Nổi Bật:</strong><br><br>"
                    + "Dành cho các buổi tiệc tối hoặc sự kiện trang trọng:<br>"
                    + "• <strong>Nữ tính & Quyến rũ:</strong> Hãy chọn ngay <a href='/products/9' class='text-danger fw-bold text-decoration-none'>Đầm body dự tiệc</a> (699.000₫) tôn dáng cực chuẩn.<br>"
                    + "• <strong>Phụ kiện điểm nhấn:</strong> Mix cùng <a href='/products/12' class='text-danger fw-bold text-decoration-none'>Túi xách da thật Mini</a> (899.000₫) chất liệu da bò sang trọng.<br>"
                    + "• <strong>Phong cách cá tính:</strong> Có thể khoác nhẹ <a href='/products/11' class='text-danger fw-bold text-decoration-none'>Áo blazer cao cấp</a> hờ trên vai để thêm phần khí chất!";
        }
        // 4. Đi chơi / Dạo phố / Đi học / Hẹn hò
        else if (lower.contains("đi chơi") || lower.contains("dạo phố") || lower.contains("đi học") || lower.contains("cafe") || lower.contains("du lịch") || lower.contains("cuối tuần") || lower.contains("hẹn hò")) {
            answer = "👟 <strong>Gợi ý Combo Dạo Phố & Hẹn Hò Năng Động (Streetwear):</strong><br><br>"
                    + "Set đồ trẻ trung, thoải mái vận động cả ngày dài:<br>"
                    + "• <strong>Áo:</strong> <a href='/products/2' class='text-danger fw-bold text-decoration-none'>Áo thun Oversize Hàn Quốc</a> (249.000₫) hoặc <a href='/products/3' class='text-danger fw-bold text-decoration-none'>Áo thun Polo Classic</a> (299.000₫).<br>"
                    + "• <strong>Quần:</strong> <a href='/products/7' class='text-danger fw-bold text-decoration-none'>Quần jean Baggy</a> (529.000₫) form rộng hot trend.<br>"
                    + "• <strong>Phụ kiện:</strong> Đội thêm chiếc <a href='/products/13' class='text-danger fw-bold text-decoration-none'>Mũ bucket thời trang</a> (159.000₫) tạo điểm nhấn đậm chất Gen Z!";
        }
        // 5. Mùa đông / Lạnh / Giữ ấm
        else if (lower.contains("mùa đông") || lower.contains("lạnh") || lower.contains("thu đông") || lower.contains("giữ ấm") || lower.contains("áo khoác") || lower.contains("bomber")) {
            answer = "❄️ <strong>Gợi ý Trang Phục Giữ Ấm & Thời Trang Thu Đông:</strong><br><br>"
                    + "• <a href='/products/10' class='text-danger fw-bold text-decoration-none'>Áo khoác Bomber phong cách</a> (599.000₫) cản gió tốt, lót trong êm ái, mặc cực kỳ tôn dáng thể thao.<br>"
                    + "• <a href='/products/11' class='text-danger fw-bold text-decoration-none'>Áo blazer công sở</a> (749.000₫) chất dạ dày dặn vừa giữ ấm vừa thanh lịch.<br>"
                    + "• Bạn có thể mặc layer bên trong một chiếc <a href='/products/1' class='text-danger fw-bold text-decoration-none'>Áo thun Basic Cotton</a> để vừa ấm vừa dễ chịu.";
        }
        // 6. Mùa hè / Nóng bức
        else if (lower.contains("mùa hè") || lower.contains("nóng") || lower.contains("mát") || lower.contains("thấm hút")) {
            answer = "☀️ <strong>Giải Nhiệt Mùa Hè Cùng Trang Phục Thoáng Mát:</strong><br><br>"
                    + "• <a href='/products/1' class='text-danger fw-bold text-decoration-none'>Áo thun Basic Cotton Premium</a> (199.000₫) chất 100% cotton dệt tự nhiên, thấm hút mồ hôi cực tốt.<br>"
                    + "• <a href='/products/8' class='text-danger fw-bold text-decoration-none'>Váy đầm hoa nhí</a> (459.000₫) vải voan nhẹ nhàng, thoáng mát, dịu dàng cho mùa hè.<br>"
                    + "• Đừng quên chiếc <a href='/products/13' class='text-danger fw-bold text-decoration-none'>Mũ bucket thời trang</a> (159.000₫) để che nắng khi dạo phố nhé!";
        }
        // 7. Giá rẻ / Ngân sách / Dưới 300k / Tiết kiệm
        else if (lower.contains("rẻ") || lower.contains("tiết kiệm") || lower.contains("sinh viên") || lower.contains("300k") || lower.contains("200k")) {
            List<Product> cheap = allProducts.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().doubleValue() <= 300000)
                    .collect(Collectors.toList());
            StringBuilder sb = new StringBuilder("🏷️ <strong>Các sản phẩm giá hạt dẻ dưới 300.000₫ cực hot:</strong><br><br>");
            for (Product p : cheap) {
                sb.append("• <a href='/products/").append(p.getId()).append("' class='text-danger fw-bold text-decoration-none'>")
                  .append(p.getName()).append("</a> — <strong>").append(priceFormat.format(p.getPrice())).append("₫</strong><br>");
            }
            sb.append("<br>💡 <em>Chất lượng cao cấp nhưng giá thành rất vừa túi tiền sinh viên bạn nhé!</em>");
            answer = sb.toString();
        }
        // 8. Chính sách đổi trả / Phí ship / Giao hàng
        else if (lower.contains("đổi trả") || lower.contains("ship") || lower.contains("vận chuyển") || lower.contains("bảo hành") || lower.contains("thanh toán")) {
            answer = "📦 <strong>Chính sách mua sắm tại Fashion Shop:</strong><br><br>"
                    + "• <strong>Phí vận chuyển:</strong> Miễn phí toàn quốc cho đơn hàng từ 499.000₫.<br>"
                    + "• <strong>Thanh toán:</strong> Hỗ trợ thanh toán khi nhận hàng (COD) — bạn được kiểm tra hàng trước khi trả tiền.<br>"
                    + "• <strong>Đổi trả:</strong> Hỗ trợ đổi size / đổi mẫu miễn phí trong vòng 30 ngày nếu còn nguyên tem mác.<br>"
                    + "• <strong>Hotline hỗ trợ:</strong> 1900 8888 (8:00 - 22:00 hàng ngày).";
        }
        // 9. Tìm kiếm trực tiếp sản phẩm trong Database theo từ khóa
        else {
            List<Product> matched = productService.searchProducts(msg);
            if (!matched.isEmpty()) {
                StringBuilder sb = new StringBuilder("🔍 <strong>Em tìm thấy " + matched.size() + " sản phẩm phù hợp với yêu cầu của bạn:</strong><br><br>");
                for (Product p : matched) {
                    sb.append("• <a href='/products/").append(p.getId()).append("' class='text-danger fw-bold text-decoration-none'>")
                      .append(p.getName()).append("</a> — Giá: <strong>").append(priceFormat.format(p.getPrice())).append("₫</strong><br>")
                      .append("&nbsp;&nbsp;<em>").append(p.getDescription() != null ? p.getDescription() : "").append("</em><br><br>");
                }
                sb.append("👉 Bạn hãy nhấp vào tên sản phẩm để xem chi tiết hoặc thêm vào giỏ hàng nhé!");
                answer = sb.toString();
            } else {
                answer = "👗 <strong>Tư vấn Stylist:</strong> Em có thể hỗ trợ bạn tìm đồ hoặc tư vấn phối trang phục theo mọi nhu cầu:<br><br>"
                        + "• <strong>Theo phong cách:</strong> Tối giản, Hàn Quốc, Streetwear, Công sở, Dạ tiệc quyến rũ.<br>"
                        + "• <strong>Theo sản phẩm:</strong> Áo thun, Polo, Sơ mi, Blazer, Quần jean, Đầm váy, Túi xách, Mũ bucket.<br>"
                        + "• <strong>Theo dịp:</strong> Đi học, đi làm, dự tiệc, đi chơi cuối tuần, phỏng vấn xin việc.<br><br>"
                        + "👉 <em>Bạn hãy nhập dịp bạn muốn mặc (ví dụ: 'đi tiệc', 'đi làm', 'áo thun đẹp') để em gợi ý cụ thể ngay nhé!</em>";
            }
        }

        res.put("answer", answer);
        return res;
    }
}
