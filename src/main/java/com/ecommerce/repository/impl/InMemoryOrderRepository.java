package com.ecommerce.repository.impl;

import com.ecommerce.model.Order;
import com.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final List<Order> store = new CopyOnWriteArrayList<>();

    @Override
    public Order save(Order order) {
        store.add(order);
        return order;
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public List<Order> findByGeoId(String geoId) {
        return store.stream()
                .filter(o -> o.getGeoId().equals(geoId))
                .collect(Collectors.toList());
    }
}
