package com.example.productcrud.service;


import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;

import java.util.List;

public interface CategoryService {

    Category getByIdAndUser(Long id, User user);

    List<Category> getCategoriesByUser(User user);

    Category save(Category category);

    Category getById(Long id);

    void delete(Long id);

    List<Category> getAll();
}