package com.example.order.serde;

import com.example.order.entity.Order;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

@NoArgsConstructor
public class OrderSerde implements Serde<Order> {

    @Override
    public Serializer<Order> serializer() {
        return new OrderSerializer();
    }

    @Override
    public Deserializer<Order> deserializer() {
        return new OrderDeserializer();
    }
}
