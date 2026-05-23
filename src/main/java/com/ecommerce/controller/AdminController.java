package com.ecommerce.controller;

import com.ecommerce.dto.request.GeoConfigRequest;
import com.ecommerce.dto.request.GenerateDiscountRequest;
import com.ecommerce.model.AnalyticsResult;
import com.ecommerce.model.DiscountCode;
import com.ecommerce.model.GeoConfig;
import com.ecommerce.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/config")
    public ResponseEntity<GeoConfig> configureGeo(@RequestBody GeoConfigRequest request) {
        GeoConfig config = adminService.configureGeo(
                request.geoId(),
                request.nthOrder(),
                request.discountPercentage()
        );
        return ResponseEntity.ok(config);
    }

    @PostMapping("/generate-discount")
    public ResponseEntity<DiscountCode> generateDiscount(@RequestBody GenerateDiscountRequest request) {
        DiscountCode code = adminService.generateDiscount(request.geoId());
        return ResponseEntity.ok(code);
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResult> getAnalytics(@RequestParam String geoId) {
        return ResponseEntity.ok(adminService.getAnalytics(geoId));
    }
}
