package com.ecommerce.repository.impl;

import com.ecommerce.model.GeoConfig;
import com.ecommerce.model.GeoState;
import com.ecommerce.repository.GeoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGeoRepository implements GeoRepository {

    private final Map<String, GeoConfig> configs = new ConcurrentHashMap<>();
    private final Map<String, GeoState> states = new ConcurrentHashMap<>();

    @Override
    public GeoConfig saveConfig(GeoConfig config) {
        configs.put(config.getGeoId(), config);
        return config;
    }

    @Override
    public Optional<GeoConfig> findConfigByGeoId(String geoId) {
        return Optional.ofNullable(configs.get(geoId));
    }

    @Override
    public GeoState saveState(GeoState state) {
        states.put(state.getGeoId(), state);
        return state;
    }

    @Override
    public GeoState findOrCreateState(String geoId) {
        return states.computeIfAbsent(geoId, id -> new GeoState(id, 0, false, null));
    }

    @Override
    public List<GeoConfig> findAllConfigs() {
        return new ArrayList<>(configs.values());
    }
}
