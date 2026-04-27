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

    // ===== UPDATE PROFILE (FINAL FIX) =====
    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String profileImageUrl,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = getCurrentUser(userDetails);

            // 🔥 HANDLE NULL + TRIM
            user.setFullName(fullName != null ? fullName.trim() : "");
            user.setEmail(email != null ? email.trim() : "");
            user.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : "");
            user.setAddress(address != null ? address.trim() : "");
            user.setBio(bio != null ? bio.trim() : "");
            user.setProfileImageUrl(profileImageUrl != null ? profileImageUrl.trim() : "");

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Profile berhasil diupdate!");
            return "redirect:/profile";

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 WAJIB lihat di console kalau error
            redirectAttributes.addFlashAttribute("error", "Gagal update profile!");
            return "redirect:/profile/edit";
        }
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
    @PostMapping("/profile/upload-image")
    public String uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "File kosong!");
                return "redirect:/profile/edit";
            }

            // (opsional tapi bagus) batasi tipe
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/jpg"))) {
                redirectAttributes.addFlashAttribute("error", "Hanya JPG/PNG!");
                return "redirect:/profile/edit";
            }

            // nama file unik
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            java.nio.file.Files.copy(
                    file.getInputStream(),
                    uploadPath.resolve(fileName),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // simpan ke user
            User user = getCurrentUser(userDetails);
            user.setProfileImageUrl("/uploads/" + fileName);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Foto berhasil diupload!");
            return "redirect:/profile";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal upload!");
            return "redirect:/profile/edit";
        }
    }
}