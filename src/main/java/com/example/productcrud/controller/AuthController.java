package com.example.productcrud.controller;

import com.example.productcrud.dto.RegisterRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.model.Category;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.CategoryService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.transaction.Transactional; // 🔥 penting

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          CategoryService categoryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryService = categoryService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    @Transactional // 🔥 WAJIB: biar user + category tersimpan dalam 1 transaksi
    public String processRegister(@ModelAttribute RegisterRequest registerRequest,
                                  RedirectAttributes redirectAttributes) {

        // ===== VALIDASI =====
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Username tidak boleh kosong!");
            return "redirect:/register";
        }

        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password tidak boleh kosong!");
            return "redirect:/register";
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password tidak cocok!");
            return "redirect:/register";
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username sudah digunakan!");
            return "redirect:/register";
        }

        // ===== SIMPAN USER =====
        User user = new User();
        user.setUsername(registerRequest.getUsername().trim());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());

        user = userRepository.save(user); // 🔥 IMPORTANT: ambil user hasil save

        System.out.println("🔥 USER CREATED: " + user.getUsername());

        // ===== BUAT CATEGORY =====
        createDefaultCategories(user);

        redirectAttributes.addFlashAttribute("success", "Registrasi berhasil! Silakan login.");
        return "redirect:/login";
    }

    private void createDefaultCategories(User user) {

        String[] defaultCategories = {
                "Elektronik",
                "Pakaian",
                "Food & Drink",
                "Mainan",
                "Kesehatan"
        };

        for (String name : defaultCategories) {
            Category c = new Category();
            c.setName(name);
            c.setUser(user);

            categoryService.save(c);

            // 🔥 DEBUG
            System.out.println("✅ Category dibuat: " + name);
        }
    }
}