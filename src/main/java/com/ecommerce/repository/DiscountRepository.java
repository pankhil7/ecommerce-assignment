package com.ecommerce.repository;

import com.ecommerce.model.DiscountCode;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository {
    DiscountCode save(DiscountCode discountCode);
    Optional<DiscountCode> findByCode(String code);
    Optional<DiscountCode> findActiveByGeoId(String geoId);
    List<DiscountCode> findAll();
    List<DiscountCode> findByGeoId(String geoId);
}
