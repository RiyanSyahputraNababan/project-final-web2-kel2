package com.example.productcrud.controller;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;
import com.example.productcrud.service.CategoryService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public DashboardController(ProductService productService,
                               CategoryService categoryService,
                               UserRepository userRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    // ✅ FIX: jangan return null
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User user = getCurrentUser(userDetails);

        List<Product> products = productService.findAll(user);

        // Total Produk
        int totalProducts = products.size();

        // Total Nilai Inventory
        long totalValue = products.stream()
                .mapToLong(p -> p.getPrice() * p.getStock())
                .sum();

        // Aktif vs Tidak Aktif
        long activeCount = products.stream()
                .filter(Product::isActive)
                .count();

        long inactiveCount = products.stream()
                .filter(p -> !p.isActive())
                .count();

        // Produk per Category
        Map<String, Long> categoryStats = products.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.counting()
                ));

        // Low Stock
        List<Product> lowStock = productService.findLowStock(user);

        // Kirim ke view
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("lowStock", lowStock);

        return "dashboard";
    }
}