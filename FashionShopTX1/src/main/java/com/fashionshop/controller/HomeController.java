package com.fashionshop.controller;

import com.fashionshop.service.ProductService;
import com.fashionshop.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "about";
    }
}
