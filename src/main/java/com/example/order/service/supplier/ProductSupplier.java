package com.example.order.service.supplier;

import com.example.order.entity.Product;

import java.util.List;

@FunctionalInterface
public interface ProductSupplier {
    List<Product> supplyProducts();
}
