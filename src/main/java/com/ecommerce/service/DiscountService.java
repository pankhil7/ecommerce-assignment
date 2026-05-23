package com.ecommerce.service;

import com.ecommerce.factory.DiscountCodeFactory;
import com.ecommerce.model.DiscountCode;
import com.ecommerce.model.GeoConfig;
import com.ecommerce.model.GeoState;
import com.ecommerce.repository.DiscountRepository;
import com.ecommerce.repository.GeoRepository;
import com.ecommerce.strategy.DiscountStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DiscountService {

    private static final Logger log = LoggerFactory.getLogger(DiscountService.class);

    private final DiscountRepository discountRepository;
    private final GeoRepository geoRepository;
    private final DiscountCodeFactory discountCodeFactory;
    private final DiscountStrategy discountStrategy;

    public DiscountService(DiscountRepository discountRepository,
                           GeoRepository geoRepository,
                           DiscountCodeFactory discountCodeFactory,
                           DiscountStrategy discountStrategy) {
        this.discountRepository = discountRepository;
        this.geoRepository = geoRepository;
        this.discountCodeFactory = discountCodeFactory;
        this.discountStrategy = discountStrategy;
    }

    // Server-side validation — does not trust any client hint
    public DiscountCode generateCode(String geoId) {
        log.info("Discount code generation requested for geo [{}]", geoId);

        GeoConfig config = geoRepository.findConfigByGeoId(geoId)
                .orElseThrow(() -> {
                    log.warn("Discount generation failed — geo not configured [{}]", geoId);
                    return new IllegalArgumentException("Geo not configured: " + geoId);
                });

        GeoState state = geoRepository.findOrCreateState(geoId);

        if (state.getOrderCount() == 0 || state.getOrderCount() % config.getNthOrder() != 0) {
            log.warn("Discount generation rejected — nth order threshold not met [geo={}, orderCount={}, n={}]",
                    geoId, state.getOrderCount(), config.getNthOrder());
            throw new IllegalStateException("Conditions not met: nth order threshold not reached for geo " + geoId);
        }

        // Enforce single active code per geo
        if (discountRepository.findActiveByGeoId(geoId).isPresent()) {
            log.warn("Discount generation rejected — active code already exists for geo [{}]", geoId);
            throw new IllegalStateException("An active discount code already exists for geo: " + geoId);
        }

        DiscountCode code = discountCodeFactory.create(geoId, state.getEligibleUserId(), config.getDiscountPercentage());

        state.setPendingCodeGeneration(false);
        geoRepository.saveState(state);

        discountRepository.save(code);
        log.info("Discount code generated [code={}, geo={}, userId={}, percentage={}%]",
                code.getCode(), geoId, code.getUserId(), code.getPercentage());
        return code;
    }

    // Validates code belongs to this geo, this user, and is unused
    public Optional<DiscountCode> validateCode(String code, String userId, String geoId) {
        log.debug("Validating discount code [code={}, userId={}, geoId={}]", code, userId, geoId);

        Optional<DiscountCode> result = discountRepository.findByCode(code)
                .filter(d -> !d.isUsed())
                .filter(d -> d.getGeoId().equals(geoId))
                .filter(d -> d.getUserId().equals(userId));

        if (result.isEmpty()) {
            log.warn("Discount code validation failed [code={}, userId={}, geoId={}]", code, userId, geoId);
        }

        return result;
    }

    public double applyDiscount(double subtotal, DiscountCode code) {
        double discount = discountStrategy.calculate(subtotal, code.getPercentage());
        log.debug("Discount applied [code={}, subtotal={}, percentage={}%, discountAmount={}]",
                code.getCode(), subtotal, code.getPercentage(), discount);
        return discount;
    }

    public void markAsUsed(DiscountCode code) {
        log.info("Marking discount code as used [code={}]", code.getCode());
        code.setUsed(true);
        discountRepository.save(code);
    }
}
