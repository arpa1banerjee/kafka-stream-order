package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final FileWriterService fileWriterService;
    private final ProductService productService;
    private final Random random;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Order buildOrder() {
        Order order = Order.builder()
                .id("Order_" + UUID.randomUUID())
                .createdAt(Instant.now().toEpochMilli())
                .lines(buildOrderLines())
                .build();
        fileWriterService.writeToJsonFile("order.json", objectMapper.writeValueAsString(order).concat(System.lineSeparator()));
        return order;
    }

    private List<Order.OrderLine> buildOrderLines() {
        List<Order.OrderLine> lines = new ArrayList<>();
        List<String> productIds = productService.getAllProductIds();
        for (int i = 0; i < (random.nextInt(30) + 1); i++) {
            lines.add(Order.OrderLine.builder()
                    .lineId("OrderLine_" + i + "_" + UUID.randomUUID())
                    .quantity(random.nextInt(10) + 1)
                    .product(Product.builder()
                                    .productId(productIds.get(random.nextInt(productIds.size())))
                                    .build())
                    .build());
        }
        return lines;
    }

    @SneakyThrows
    public void saveOrders(final Order order, final String fileName) {
        fileWriterService.writeToJsonFile(fileName, objectMapper.writeValueAsString(order).concat(System.lineSeparator()));
    }
}
