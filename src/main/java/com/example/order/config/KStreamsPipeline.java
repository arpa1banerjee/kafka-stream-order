package com.example.order.config;

import com.example.order.entity.Order;
import com.example.order.entity.Product;
import com.example.order.service.OrderService;
import com.example.order.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KStreamsPipeline {

    private final OrderService orderService;
    private final ProductService productService;

    @Bean
    public Supplier<Order> orderSupplier() {
        return () -> {
            log.info("supplying order");
            return orderService.buildOrder();
        };
    }

    @Bean
    public Function<KStream<String, Order>, KStream<String, Order>> orderEnricher() {
        return order -> {
            return order
                    .filter((key, value) -> !Objects.isNull(value.getId()))
                    .mapValues(value -> {
                        value.setLines(value.getLines().stream()
                                .peek(orderLine -> orderLine.setProduct(productService.productDetails(orderLine.getProduct().getProductId())))
                                .collect(Collectors.toList()));
                        return value;
                    });
        };
    }

    @Bean
    public Function<KStream<String, Order>, KStream<String, Order>[]> orderSegregator() {
        return order -> {
            order.foreach((key, value) -> log.info("processing order {} with orderline size {} ", value.getId(), value.getLines().size()));

            order.map((key, value) -> {
                if (value.getLines().size() <= 5) {
                    return new KeyValue<>("smallOrders", "0");
                } else if (value.getLines().size() > 5 && value.getLines().size() <= 10) {
                    return new KeyValue<>("mediumOrders", "0");
                } else {
                    return new KeyValue<>("largeOrders", "0");
                }
            })
            .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
            .count(Materialized.as("orders-materialized-view"));

            Predicate<Object, Order> isSmallOrders = (k, v) -> v.getLines().size() <= 5;
            Predicate<Object, Order> isMediumOrders = (k, v) -> v.getLines().size() > 5 && v.getLines().size() <= 10;
            Predicate<Object, Order> isLargeOrders = (k, v) -> v.getLines().size() > 10;

            return order
                    .branch(isSmallOrders, isMediumOrders, isLargeOrders);
        };
    }

    @Bean
    public Consumer<KStream<String, Order>> smallOrderSink() {
        return order -> {
            order.foreach((key, value) -> {
                log.info("consumed small order: {}", value);
                orderService.saveOrders(value, "small-orders.json");
            });
        };

    }

    @Bean
    public Consumer<KStream<String, Order>> mediumOrderSink() {
        return order -> {
            order.foreach((key, value) -> {
                log.info("consumed medium order: {}", value);
                orderService.saveOrders(value, "medium-orders.json");
            });
        };

    }

    @Bean
    public Consumer<KStream<String, Order>> largeOrderSink() {
        return order -> {
            order.foreach((key, value) -> {
                log.info("consumed large order: {}", value);
                orderService.saveOrders(value, "large-orders.json");
            });
        };

    }
}
