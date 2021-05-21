package com.example.order.serde;

import com.example.order.entity.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class OrderSerializer implements Serializer<Order> {
    @Override
    public byte[] serialize(String s, Order order) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(order).getBytes();
        } catch (Exception e) {
            log.error("serialisation exception", e);
        }
        return retVal;
    }
}
