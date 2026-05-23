package com.ecommerce.repository;

import com.ecommerce.model.GeoConfig;
import com.ecommerce.model.GeoState;
import java.util.List;
import java.util.Optional;

public interface GeoRepository {
    GeoConfig saveConfig(GeoConfig config);
    Optional<GeoConfig> findConfigByGeoId(String geoId);
    GeoState saveState(GeoState state);
    GeoState findOrCreateState(String geoId);
    List<GeoConfig> findAllConfigs();
}
