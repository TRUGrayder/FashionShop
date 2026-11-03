package com.fashionshop.controller;

import com.fashionshop.model.Product;
import com.fashionshop.service.ProductService;
import com.fashionshop.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       Model model,
                       HttpSession session) {
        java.util.List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchProducts(keyword);
            model.addAttribute("keyword", keyword.trim());
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId);
            model.addAttribute("selectedCategory", categoryId);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "product/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "product/detail";
    }
}
