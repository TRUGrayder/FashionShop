package com.fashionshop.controller;

import com.fashionshop.model.User;
import com.fashionshop.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/ai")
public class AiController {

    private final ProductService productService;

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
        String lower = message.toLowerCase();
        String answer;

        // Logic tư vấn thời trang đơn giản (sẵn sàng thay bằng Qdrant + RAG)
        if (lower.contains("áo thun") || lower.contains("ao thun")) {
            answer = "Gợi ý của em: Bạn có thể tham khảo **Áo thun Basic Cotton Premium** (199.000đ) hoặc **Áo thun Oversize Hàn Quốc** (249.000đ). Form đẹp, chất cotton mềm, rất dễ phối đồ!";
        } else if (lower.contains("váy") || lower.contains("đầm") || lower.contains("dam")) {
            answer = "Với váy đầm, em gợi ý **Váy đầm hoa nhí** (459.000đ) rất nữ tính hoặc **Đầm body dự tiệc** (699.000đ) nếu bạn cần dự tiệc tối.";
        } else if (lower.contains("jean") || lower.contains("quần")) {
            answer = "Quần jean đang hot: **Quần jean Slim Fit** (499.000đ) tôn dáng, hoặc **Quần jean Baggy** (529.000đ) theo trend form rộng.";
        } else if (lower.contains("công sở") || lower.contains("đi làm")) {
            answer = "Cho môi trường công sở, em khuyên **Áo sơ mi Oxford trắng** + **Áo blazer công sở**. Thanh lịch và chuyên nghiệp!";
        } else if (lower.contains("xin chào") || lower.contains("hello") || lower.contains("hi")) {
            answer = "Xin chào! Em là trợ lý AI của Fashion Shop. Bạn muốn tư vấn phối đồ, chọn trang phục theo dịp, hay tìm sản phẩm cụ thể nào ạ?";
        } else {
            answer = "Em đã nhận được câu hỏi của bạn. Hiện tại em có thể tư vấn về áo thun, sơ mi, quần jean, váy đầm, đồ công sở... Bạn cứ hỏi cụ thể hơn nhé! (Module Qdrant đang được tích hợp để tư vấn thông minh hơn)";
        }

        res.put("answer", answer);
        return res;
    }
}
