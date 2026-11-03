package com.fashionshop.controller;

import com.fashionshop.model.Order;
import com.fashionshop.model.User;
import com.fashionshop.service.OrderService;
import com.fashionshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    private User getSessionUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @GetMapping
    public String viewProfile(Model model, HttpSession session) {
        User sessionUser = getSessionUser(session);
        if (sessionUser == null) {
            return "redirect:/login";
        }

        User user = userService.findById(sessionUser.getId());
        if (user == null) {
            user = sessionUser;
        }

        List<Order> orders = orderService.getOrdersByUser(user.getId());
        BigDecimal totalSpent = orders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("totalSpent", totalSpent);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                HttpSession session,
                                RedirectAttributes ra) {
        User sessionUser = getSessionUser(session);
        if (sessionUser == null) {
            return "redirect:/login";
        }

        User user = userService.findById(sessionUser.getId());
        if (user != null) {
            user.setFullName(fullName != null ? fullName.trim() : "");
            user.setEmail(email != null && !email.isBlank() ? email.trim() : null);
            user.setPhone(phone != null && !phone.isBlank() ? phone.trim() : null);
            user.setAddress(address != null && !address.isBlank() ? address.trim() : null);

            if (userService.updateProfile(user)) {
                session.setAttribute("currentUser", user);
                ra.addFlashAttribute("success", "Cập nhật thông tin cá nhân thành công!");
            } else {
                ra.addFlashAttribute("error", "Cập nhật thông tin thất bại!");
            }
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        User sessionUser = getSessionUser(session);
        if (sessionUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("passwordError", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            ra.addFlashAttribute("passwordError", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/profile";
        }

        if (userService.changePassword(sessionUser.getId(), oldPassword, newPassword)) {
            ra.addFlashAttribute("passwordSuccess", "Đổi mật khẩu thành công!");
        } else {
            ra.addFlashAttribute("passwordError", "Mật khẩu hiện tại không chính xác!");
        }
        return "redirect:/profile";
    }
}
