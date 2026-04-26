package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.productcrud.repository.UserRepository;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(CategoryService categoryService,
                              UserRepository userRepository) {

        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(Model model) {

        User user = getCurrentUser();

        model.addAttribute("categories",
                categoryService.getCategoriesByUser(user));

        return "category/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        User user = getCurrentUser();

        Category category = categoryService.getByIdAndUser(id, user);

        if (category == null) {
            return "redirect:/categories";
        }

        model.addAttribute("category", category);

        return "category/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        User user = getCurrentUser();

        Category category = categoryService.getByIdAndUser(id, user);

        if (category != null) {
            categoryService.delete(category.getId());
        }

        return "redirect:/categories";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category) {

        User user = getCurrentUser();

        category.setUser(user);

        categoryService.save(category);

        return "redirect:/categories";
    }

    private User getCurrentUser() {

        UserDetails userDetails = (UserDetails)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        String username = userDetails.getUsername();

        return userRepository.findByUsername(username).orElse(null);
    }
}