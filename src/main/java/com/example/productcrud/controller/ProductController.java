package com.example.productcrud.controller;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;
import com.example.productcrud.service.CategoryService;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             UserRepository userRepository) {

        this.productService = productService;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    // ===== Helper Ambil User Login =====
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // ===== Redirect Home =====
    @GetMapping("/")
    public String index() {
        return "redirect:/products";
    }

    // ===== List Product =====
    @GetMapping("/products")
    public String listProducts(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User currentUser = getCurrentUser(userDetails);

        model.addAttribute("products",
                productService.findAllByOwner(currentUser));

        return "product/list";
    }

    // ===== Detail Product =====
    @GetMapping("/products/{id}")
    public String detailProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Produk tidak ditemukan.");
                    return "redirect:/products";
                });
    }

    // ===== Form Tambah Product =====
    @GetMapping("/products/new")
    public String showCreateForm(Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        Product product = new Product();
        product.setCreatedAt(LocalDate.now());

        User user = getCurrentUser(userDetails);

        model.addAttribute("product", product);
        model.addAttribute("categories",
                categoryService.getCategoriesByUser(user));

        return "product/form";
    }

    // ===== Simpan Product =====
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        // Validasi ownership saat edit
        if (product.getId() != null) {
            boolean isOwner = productService
                    .findByIdAndOwner(product.getId(), currentUser)
                    .isPresent();

            if (!isOwner) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Produk tidak ditemukan.");
                return "redirect:/products";
            }
        }

        product.setOwner(currentUser);
        productService.save(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "Produk berhasil disimpan!");

        return "redirect:/products";
    }

    // ===== Form Edit Product =====
    @GetMapping("/products/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {

                    model.addAttribute("product", product);

                    model.addAttribute("categories",
                            categoryService.getCategoriesByUser(currentUser));

                    return "product/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Produk tidak ditemukan.");
                    return "redirect:/products";
                });
    }

    // ===== Delete Product =====
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        boolean isOwner = productService
                .findByIdAndOwner(id, currentUser)
                .isPresent();

        if (isOwner) {
            productService.deleteByIdAndOwner(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Produk berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Produk tidak ditemukan.");
        }

        return "redirect:/products";
    }
}