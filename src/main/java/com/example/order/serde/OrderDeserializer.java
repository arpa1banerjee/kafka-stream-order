package com.example.order.serde;

import com.example.order.entity.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class OrderDeserializer implements Deserializer<Order> {
    @Override
    public Order deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        Order order = null;
        try {
            order = mapper.readValue(bytes, Order.class);
        } catch (Exception e) {
            log.error("deserialisation exception", e);
        }
        return order;
    }
}
