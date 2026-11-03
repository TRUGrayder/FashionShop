package com.fashionshop.controller;

import com.fashionshop.model.User;
import com.fashionshop.service.CartService;
import com.fashionshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    private User getUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        var items = cartService.getCartItems(user.getId());
        if (items.isEmpty()) return "redirect:/cart";

        model.addAttribute("cartItems", items);
        model.addAttribute("total", cartService.getTotal(user.getId()));
        model.addAttribute("currentUser", user);
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String shippingAddress,
                             @RequestParam String phone,
                             @RequestParam(required = false) String note,
                             HttpSession session,
                             RedirectAttributes ra) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        Long orderId = orderService.placeOrder(user.getId(), shippingAddress, phone, note);
        if (orderId != null) {
            ra.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn: #" + orderId);
            return "redirect:/orders";
        }
        ra.addFlashAttribute("error", "Đặt hàng thất bại!");
        return "redirect:/checkout";
    }

    @GetMapping("/orders")
    public String myOrders(Model model, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("orders", orderService.getOrdersByUser(user.getId()));
        model.addAttribute("currentUser", user);
        return "order/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        var order = orderService.getOrderById(id);
        if (order == null || !order.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("currentUser", user);
        return "order/order-detail";
    }
}
