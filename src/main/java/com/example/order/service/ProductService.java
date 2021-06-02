package com.example.order.service;

import com.example.order.entity.Product;
import com.example.order.service.supplier.ProductSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {

    ProductSupplier productSupplier = () -> Arrays.asList(
                Product.builder().productId("P1").productName("Coke").build(),
                Product.builder().productId("P2").productName("Chicken").build(),
                Product.builder().productId("P3").productName("Milk").build(),
                Product.builder().productId("P4").productName("Butter").build(),
                Product.builder().productId("P5").productName("Bread").build()
        );


    public List<String> getAllProductIds() {

        return productSupplier.supplyProducts().stream().map(Product::getProductId).collect(Collectors.toList());
    }
    public Product productDetails(final String productId) {
        return productSupplier.supplyProducts().stream().filter(item -> item.getProductId().equals(productId)).findAny().orElseGet(Product::new);
    }

}
