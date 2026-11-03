package com.fashionshop.controller;

import com.fashionshop.model.User;
import com.fashionshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private User getUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("cartItems", cartService.getCartItems(user.getId()));
        model.addAttribute("total", cartService.getTotal(user.getId()));
        model.addAttribute("currentUser", user);
        model.addAttribute("cartCount", cartService.countItems(user.getId()));
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes ra) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        boolean ok = cartService.addToCart(user.getId(), productId, quantity);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Đã thêm vào giỏ hàng!" : "Thêm vào giỏ thất bại!");
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam int quantity,
                                 HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";
        cartService.updateQuantity(user.getId(), productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/api/update")
    @ResponseBody
    public Map<String, Object> updateQuantityAjax(@RequestParam Long productId,
                                                  @RequestParam int quantity,
                                                  HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        User user = getUser(session);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Chưa đăng nhập");
            return res;
        }

        if (quantity <= 0) {
            cartService.removeItem(user.getId(), productId);
        } else {
            cartService.updateQuantity(user.getId(), productId, quantity);
        }

        BigDecimal total = cartService.getTotal(user.getId());
        int count = cartService.countItems(user.getId());

        BigDecimal itemSubTotal = BigDecimal.ZERO;
        for (var item : cartService.getCartItems(user.getId())) {
            if (item.getProductId().equals(productId)) {
                itemSubTotal = item.getSubTotal();
                break;
            }
        }

        res.put("success", true);
        res.put("quantity", quantity);
        res.put("subTotal", itemSubTotal);
        res.put("total", total);
        res.put("cartCount", count);
        return res;
    }

    @GetMapping("/remove/{productId}")
    public String removeItem(@PathVariable Long productId, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";
        cartService.removeItem(user.getId(), productId);
        return "redirect:/cart";
    }
}
