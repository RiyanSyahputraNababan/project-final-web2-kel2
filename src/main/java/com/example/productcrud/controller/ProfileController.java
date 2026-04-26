package com.example.productcrud.controller;

import com.example.productcrud.dto.ChangePasswordRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ HANYA SATU CONSTRUCTOR
    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // ===== VIEW PROFILE =====
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        return "profile";
    }

    // ===== EDIT PROFILE =====
    @GetMapping("/profile/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        return "profile-edit";
    }

    // ===== UPDATE PROFILE =====
    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String address,
            @RequestParam String bio,
            @RequestParam String profileImageUrl
    ) {
        User user = getCurrentUser(userDetails);

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setBio(bio);
        user.setProfileImageUrl(profileImageUrl);

        userRepository.save(user);

        return "redirect:/profile";
    }

    // ===== CHANGE PASSWORD =====
    @GetMapping("/profile/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("request", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute ChangePasswordRequest request,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(userDetails);

        // VALIDASI PASSWORD LAMA
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password lama salah!");
            return "redirect:/profile/change-password";
        }

        // VALIDASI KONFIRMASI
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi password tidak cocok!");
            return "redirect:/profile/change-password";
        }

        // UPDATE PASSWORD
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Password berhasil diubah!");
        return "redirect:/profile";
    }
}