package com.fashionshop.controller;

import com.fashionshop.model.User;
import com.fashionshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes ra) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("currentUser", user);
            ra.addFlashAttribute("success", "Đăng nhập thành công!");
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
        }
        ra.addFlashAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam(required = false) String email,
                           RedirectAttributes ra) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng điền đầy đủ Họ tên, Tên đăng nhập và Mật khẩu!");
            return "redirect:/register";
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setFullName(fullName.trim());
        user.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
        user.setRole("USER");

        if (userService.register(user)) {
            ra.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        }
        ra.addFlashAttribute("error", "Tên đăng nhập '" + username.trim() + "' đã tồn tại! Vui lòng chọn tên khác.");
        return "redirect:/register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
