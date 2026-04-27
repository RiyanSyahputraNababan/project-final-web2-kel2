package com.example.productcrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.CategoryService;

@SpringBootApplication
public class ProductCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductCrudApplication.class, args);
    }

    @Bean
    CommandLineRunner initCategories(CategoryService categoryService,
                                     UserRepository userRepository) {
        return args -> {


            User user = userRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (user != null) {


                if (categoryService.getCategoriesByUser(user).isEmpty()) {

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
                    }

                    System.out.println("✅ Default category berhasil dibuat");
                }
            } else {
                System.out.println("⚠️ Tidak ada user di database");
            }
        };
    }
}