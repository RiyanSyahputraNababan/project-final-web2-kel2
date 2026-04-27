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

import java.util.*;
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

    // 🔒 Ambil user login (aman)
    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User belum login");
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        try {
            User user = getCurrentUser(userDetails);

            // Ambil semua produk user
            List<Product> products = productService.findAll(user) != null
                    ? productService.findAll(user)
                    : new ArrayList<>();

            System.out.println("===== DEBUG DASHBOARD =====");
            System.out.println("USER: " + user.getUsername());
            System.out.println("JUMLAH PRODUCT: " + (products != null ? products.size() : "NULL"));

            // ===== TOTAL PRODUK =====
            int totalProducts = (products != null) ? products.size() : 0;

            // ===== TOTAL NILAI INVENTORY (AMAN DARI NULL) =====
            long totalValue = (products != null) ? products.stream()
                    .mapToLong(p -> p.getPrice() * p.getStock())
                    .sum() : 0;

            // ===== STATUS =====
            long activeCount = (products != null) ? products.stream()
                    .filter(p -> p.isActive())
                    .count() : 0;

            long inactiveCount = (products != null) ? products.stream()
                    .filter(p -> !p.isActive())
                    .count() : 0;

            // ===== CATEGORY STATS (ANTI NULL) =====
            Map<String, Long> categoryStats = (products != null) ? products.stream()
                    .filter(p -> p.getCategory() != null)
                    .collect(Collectors.groupingBy(
                            p -> {
                                String name = p.getCategory().getName();
                                return (name != null) ? name : "Tanpa Kategori";
                            },
                            Collectors.counting()
                    )) : new HashMap<>();

            // ===== LOW STOCK (AMAN) =====
            List<Product> lowStock;
            try {
                lowStock = productService.findLowStock(user);
            } catch (Exception e) {
                // fallback kalau service error
                lowStock = (products != null) ? products.stream()
                        .filter(p -> p.getStock() < 5)
                        .toList()
                        : new ArrayList<>();
            }

            // ===== KIRIM KE VIEW =====
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalValue", totalValue);
            model.addAttribute("activeCount", activeCount);
            model.addAttribute("inactiveCount", inactiveCount);
            model.addAttribute("categoryStats", categoryStats);
            model.addAttribute("lowStock", lowStock);

            return "dashboard";

        } catch (Exception e) {
            e.printStackTrace();

            // fallback kalau error
            return "redirect:/products";
        }
    }
}