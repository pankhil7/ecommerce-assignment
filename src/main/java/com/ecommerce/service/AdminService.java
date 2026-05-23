package com.ecommerce.service;

import com.ecommerce.model.AnalyticsResult;
import com.ecommerce.model.DiscountCode;
import com.ecommerce.model.GeoConfig;
import com.ecommerce.model.Order;
import com.ecommerce.repository.DiscountRepository;
import com.ecommerce.repository.GeoRepository;
import com.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final GeoRepository geoRepository;
    private final OrderRepository orderRepository;
    private final DiscountRepository discountRepository;
    private final DiscountService discountService;

    public AdminService(GeoRepository geoRepository,
                        OrderRepository orderRepository,
                        DiscountRepository discountRepository,
                        DiscountService discountService) {
        this.geoRepository = geoRepository;
        this.orderRepository = orderRepository;
        this.discountRepository = discountRepository;
        this.discountService = discountService;
    }

    public GeoConfig configureGeo(String geoId, int nthOrder, double discountPercentage) {
        // If config already exists, update it in place (new n applies immediately)
        GeoConfig config = geoRepository.findConfigByGeoId(geoId)
                .orElse(new GeoConfig(geoId, nthOrder, discountPercentage));
        config.setNthOrder(nthOrder);
        config.setDiscountPercentage(discountPercentage);
        geoRepository.saveConfig(config);
        log.info("Geo configured [geoId={}, nthOrder={}, discountPercentage={}%]", geoId, nthOrder, discountPercentage);
        return config;
    }

    public DiscountCode generateDiscount(String geoId) {
        log.info("Admin requested discount code generation [geoId={}]", geoId);
        return discountService.generateCode(geoId);
    }

    public AnalyticsResult getAnalytics(String geoId) {
        log.info("Analytics requested [geoId={}]", geoId);
        List<Order> orders = orderRepository.findByGeoId(geoId);
        List<DiscountCode> codes = discountRepository.findByGeoId(geoId);

        double totalRevenue = orders.stream().mapToDouble(Order::getTotal).sum();
        double totalDiscountAmount = orders.stream().mapToDouble(Order::getDiscountAmount).sum();

        log.debug("Analytics result [geoId={}, totalOrders={}, totalRevenue={}, totalDiscounts={}]",
                geoId, orders.size(), totalRevenue, totalDiscountAmount);
        return new AnalyticsResult(orders.size(), totalRevenue, totalDiscountAmount, codes);
    }
}
