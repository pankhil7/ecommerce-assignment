package com.ecommerce.service;

import com.ecommerce.factory.DiscountCodeFactory;
import com.ecommerce.model.DiscountCode;
import com.ecommerce.model.GeoConfig;
import com.ecommerce.model.GeoState;
import com.ecommerce.repository.impl.InMemoryDiscountRepository;
import com.ecommerce.repository.impl.InMemoryGeoRepository;
import com.ecommerce.strategy.PercentageDiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DiscountServiceTest {

    private DiscountService discountService;
    private InMemoryGeoRepository geoRepository;
    private InMemoryDiscountRepository discountRepository;

    @BeforeEach
    void setUp() {
        geoRepository = new InMemoryGeoRepository();
        discountRepository = new InMemoryDiscountRepository();
        discountService = new DiscountService(
                discountRepository,
                geoRepository,
                new DiscountCodeFactory(),
                new PercentageDiscountStrategy()
        );
    }

    private void setupGeo(String geoId, int n, double pct, int orderCount, String eligibleUser) {
        geoRepository.saveConfig(new GeoConfig(geoId, n, pct));
        GeoState state = geoRepository.findOrCreateState(geoId);
        state.setOrderCount(orderCount);
        state.setEligibleUserId(eligibleUser);
        geoRepository.saveState(state);
    }

    @Test
    void generateCode_whenNthOrderReached_returnsCode() {
        setupGeo("US", 5, 10.0, 5, "user1");

        DiscountCode code = discountService.generateCode("US");

        assertNotNull(code.getCode());
        assertEquals("US", code.getGeoId());
        assertEquals("user1", code.getUserId());
        assertEquals(10.0, code.getPercentage());
        assertFalse(code.isUsed());
    }

    @Test
    void generateCode_whenConditionsNotMet_throwsException() {
        setupGeo("US", 5, 10.0, 3, null);

        assertThrows(IllegalStateException.class, () -> discountService.generateCode("US"));
    }

    @Test
    void generateCode_whenActiveCodeExists_throwsException() {
        setupGeo("US", 5, 10.0, 5, "user1");
        discountService.generateCode("US"); // first code

        // Reset order count to 10 so condition is met again
        GeoState state = geoRepository.findOrCreateState("US");
        state.setOrderCount(10);
        state.setEligibleUserId("user2");
        geoRepository.saveState(state);

        assertThrows(IllegalStateException.class, () -> discountService.generateCode("US"));
    }

    @Test
    void generateCode_geoNotConfigured_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> discountService.generateCode("IN"));
    }

    @Test
    void validateCode_validCodeCorrectUser_returnsCode() {
        setupGeo("US", 5, 10.0, 5, "user1");
        DiscountCode code = discountService.generateCode("US");

        Optional<DiscountCode> result = discountService.validateCode(code.getCode(), "user1", "US");

        assertTrue(result.isPresent());
    }

    @Test
    void validateCode_wrongUser_returnsEmpty() {
        setupGeo("US", 5, 10.0, 5, "user1");
        DiscountCode code = discountService.generateCode("US");

        Optional<DiscountCode> result = discountService.validateCode(code.getCode(), "user2", "US");

        assertFalse(result.isPresent());
    }

    @Test
    void validateCode_wrongGeo_returnsEmpty() {
        setupGeo("US", 5, 10.0, 5, "user1");
        DiscountCode code = discountService.generateCode("US");

        Optional<DiscountCode> result = discountService.validateCode(code.getCode(), "user1", "IN");

        assertFalse(result.isPresent());
    }

    @Test
    void validateCode_usedCode_returnsEmpty() {
        setupGeo("US", 5, 10.0, 5, "user1");
        DiscountCode code = discountService.generateCode("US");
        discountService.markAsUsed(code);

        Optional<DiscountCode> result = discountService.validateCode(code.getCode(), "user1", "US");

        assertFalse(result.isPresent());
    }

    @Test
    void applyDiscount_calculatesPercentageCorrectly() {
        setupGeo("US", 5, 10.0, 5, "user1");
        DiscountCode code = discountService.generateCode("US");

        double discount = discountService.applyDiscount(200.0, code);

        assertEquals(20.0, discount, 0.001);
    }
}
