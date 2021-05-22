package com.example.order.controller;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/order")
public class OrderResource {

    private final InteractiveQueryService interactiveQueryService;

    @GetMapping("/counts")
    Map<String, Long> counts() {
        Map<String, Long> counts = new HashMap<>();
        ReadOnlyKeyValueStore<String, Long> queryableStoreType =
                this.interactiveQueryService.getQueryableStore("orders-materialized-view", QueryableStoreTypes.keyValueStore());
        KeyValueIterator<String, Long> all = queryableStoreType.all();
        while (all.hasNext()) {
            KeyValue<String, Long> value = all.next();
            counts.put(value.key, value.value);
        }
        return counts;
    }

    @GetMapping("/windowed/{duration}/counts")
    Map<String, Long> lastFiveMinCounts(@PathVariable int duration) {
        Instant timeTo = Instant.now();
        Instant timeFrom = Instant.now().minus(duration, ChronoUnit.MINUTES);
        Map<String, Long> counts = new HashMap<>();
        ReadOnlyWindowStore<String, Long> queryableStoreType =
                this.interactiveQueryService.getQueryableStore("orders-materialized-view-time-windowed", QueryableStoreTypes.windowStore());
        KeyValueIterator<Windowed<String>, Long> windowedIterator = queryableStoreType.fetchAll(timeFrom, timeTo);
        String keyString = "";
        while (windowedIterator.hasNext()) {
            KeyValue<Windowed<String>, Long> value = windowedIterator.next();
            Windowed<String> key = value.key;
            if (key != null) {
                keyString = String.format("%s", ((Windowed) key).key());
            }
            counts.put(keyString, value.value);
        }
        return counts;
    }

}
