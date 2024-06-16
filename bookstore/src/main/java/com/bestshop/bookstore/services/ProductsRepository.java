package com.bestshop.bookstore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bestshop.bookstore.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {
}


