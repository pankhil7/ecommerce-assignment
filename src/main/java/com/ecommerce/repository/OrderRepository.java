package com.ecommerce.repository;

import com.ecommerce.model.Order;
import java.util.List;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findAll();
    List<Order> findByGeoId(String geoId);
}
