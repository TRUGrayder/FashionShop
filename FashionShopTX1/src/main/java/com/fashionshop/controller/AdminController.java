package com.fashionshop.controller;

import com.fashionshop.model.Order;
import com.fashionshop.model.Product;
import com.fashionshop.model.User;
import com.fashionshop.service.ProductService;
import com.fashionshop.service.CategoryService;
import com.fashionshop.service.UserService;
import com.fashionshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;

    public AdminController(ProductService productService,
                           CategoryService categoryService,
                           UserService userService,
                           OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.orderService = orderService;
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        List<Order> orders = orderService.getAllOrders();
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("productCount", productService.countProducts());
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String manageOrders(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/orders";
    }

    @PostMapping("/orders/status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam String status,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";

        boolean success = orderService.updateStatus(orderId, status);
        ra.addFlashAttribute(success ? "success" : "error",
                success ? "Cập nhật trạng thái đơn #" + orderId + " thành công!" : "Cập nhật thất bại!");
        return "redirect:/admin/orders";
    }

    @GetMapping("/products")
    public String manageProducts(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addForm(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam BigDecimal price,
                              @RequestParam Integer quantity,
                              @RequestParam(required = false) String image,
                              @RequestParam Long categoryId,
                              RedirectAttributes ra,
                              HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setImage(image);
        product.setCategoryId(categoryId);

        boolean success;
        if (id == null) {
            success = productService.createProduct(product);
            ra.addFlashAttribute(success ? "success" : "error",
                    success ? "Thêm sản phẩm thành công!" : "Thêm sản phẩm thất bại!");
        } else {
            success = productService.updateProduct(product);
            ra.addFlashAttribute(success ? "success" : "error",
                    success ? "Cập nhật sản phẩm thành công!" : "Cập nhật thất bại!");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/admin/products";

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/product-form";
    }

    @GetMapping("/products/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        boolean success = productService.deleteProduct(id);
        ra.addFlashAttribute(success ? "success" : "error",
                success ? "Xóa sản phẩm thành công!" : "Xóa thất bại!");
        return "redirect:/admin/products";
    }
}
