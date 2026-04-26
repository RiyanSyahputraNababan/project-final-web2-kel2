package com.example.productcrud.service;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllByOwner(User owner) {
        return productRepository.findByOwner(owner);
    }

    public Optional<Product> findByIdAndOwner(Long id, User owner) {
        return productRepository.findByIdAndOwner(id, owner);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteByIdAndOwner(Long id, User owner) {
        productRepository.findByIdAndOwner(id, owner)
                .ifPresent(product -> productRepository.delete(product));
    }

    public Page<Product> search(User user, String keyword, Long categoryId, Pageable pageable) {
        return productRepository.search(user, keyword, categoryId, pageable);
    }

    public List<Product> findLowStock(User user) {
        return productRepository.findByOwnerAndStockLessThan(user, 5);
    }

    public List<Product> findAll(User user) {
        return productRepository.findByOwner(user);
    }

}