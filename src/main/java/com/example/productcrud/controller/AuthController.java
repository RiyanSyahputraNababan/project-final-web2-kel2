package com.example.productcrud.controller;

import com.example.productcrud.dto.RegisterRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            redirectAttributes.addFlashAttribute("error", "Password dan konfirmasi password tidak cocok!");
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

        // 🔥 TAMBAHAN (PROFILE FIELD)
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Registrasi berhasil! Silakan login.");
        return "redirect:/login";
    }
}