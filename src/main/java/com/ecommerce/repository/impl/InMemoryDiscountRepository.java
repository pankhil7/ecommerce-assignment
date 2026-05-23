package com.ecommerce.repository.impl;

import com.ecommerce.model.DiscountCode;
import com.ecommerce.repository.DiscountRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryDiscountRepository implements DiscountRepository {

    private final Map<String, DiscountCode> store = new ConcurrentHashMap<>();

    @Override
    public DiscountCode save(DiscountCode discountCode) {
        store.put(discountCode.getCode(), discountCode);
        return discountCode;
    }

    @Override
    public Optional<DiscountCode> findByCode(String code) {
        return Optional.ofNullable(store.get(code));
    }

    @Override
    public Optional<DiscountCode> findActiveByGeoId(String geoId) {
        return store.values().stream()
                .filter(d -> d.getGeoId().equals(geoId) && !d.isUsed())
                .findFirst();
    }

    @Override
    public List<DiscountCode> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<DiscountCode> findByGeoId(String geoId) {
        return store.values().stream()
                .filter(d -> d.getGeoId().equals(geoId))
                .collect(Collectors.toList());
    }
}
