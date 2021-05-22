package com.example.order.config;

import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import com.example.order.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Objects;
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
        return order -> order
                // filtering out bad orders
                .filter((key, value) -> !Objects.isNull(value.getId()))
                // enriching product details
                .mapValues(value -> {
                    value.setLines(value.getLines().stream()
                            .peek(orderLine -> orderLine.setProduct(productService.productDetails(orderLine.getProduct().getProductId())))
                            .collect(Collectors.toList()));
                    return value;
                });
    }

    @Bean
    public Function<KStream<String, Order>, KStream<String, Order>[]> orderSegregator() {
        return order -> {
            order.foreach((key, value) -> log.info("processing order {} with orderLine size {} ", value.getId(), value.getLines().size()));
            // stateful operation - count of segregated order into a materialized view
            order.map((key, value) -> orderService.segregateOrderBySize(value))
                    .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                    .count(Materialized.as("orders-materialized-view"));
            // stateful operation - time windowed
            order.map((key, value) -> orderService.segregateOrderBySize(value))
                    .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                    .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
                    .count(Materialized.as("orders-materialized-view-time-windowed"));

            // branching out orders based on orderLine size
            Predicate<Object, Order> isSmallOrders = (k, v) -> v.getLines().size() <= 5;
            Predicate<Object, Order> isMediumOrders = (k, v) -> v.getLines().size() > 5 && v.getLines().size() <= 10;
            Predicate<Object, Order> isLargeOrders = (k, v) -> v.getLines().size() > 10;

            return order
                    .branch(isSmallOrders, isMediumOrders, isLargeOrders);
        };
    }

    @Bean
    public Consumer<KStream<String, Order>> smallOrderSink() {
        return order -> order.foreach((key, value) -> {
            log.info("consumed small order: {}", value);
            orderService.saveOrders(value, "small-orders.json");
        });

    }

    @Bean
    public Consumer<KStream<String, Order>> mediumOrderSink() {
        return order -> order.foreach((key, value) -> {
            log.info("consumed medium order: {}", value);
            orderService.saveOrders(value, "medium-orders.json");
        });

    }

    @Bean
    public Consumer<KStream<String, Order>> largeOrderSink() {
        return order -> order.foreach((key, value) -> {
            log.info("consumed large order: {}", value);
            orderService.saveOrders(value, "large-orders.json");
        });

    }
}
