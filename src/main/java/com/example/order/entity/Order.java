package com.example.order.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private long createdAt;
    private List<OrderLine> lines;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderLine {
        private String lineId;
        private double quantity;
        private Product product;
    }
}
