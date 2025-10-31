package com.location.management.controller;

import com.location.management.config.TenantContext;
import com.location.management.dto.CityDTO;
import com.location.management.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Tag(name = "City Management", description = "APIs for managing cities")
public class CityController {

    private final CityService cityService;

    @GetMapping
    @Operation(summary = "Get all cities", description = "Retrieve all cities with pagination")
    public ResponseEntity<Page<CityDTO>> getAllCities(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<CityDTO> cities = cityService.getAllCities(tenantId, pageable);
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get city by ID", description = "Retrieve a specific city by its ID")
    public ResponseEntity<CityDTO> getCityById(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        CityDTO city = cityService.getCityById(id, tenantId);
        return ResponseEntity.ok(city);
    }

    @PostMapping
    @Operation(summary = "Create city", description = "Create a new city")
    public ResponseEntity<CityDTO> createCity(@Valid @RequestBody CityDTO cityDTO) {
        String tenantId = TenantContext.getTenantId();
        CityDTO createdCity = cityService.createCity(cityDTO, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update city", description = "Update an existing city")
    public ResponseEntity<CityDTO> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityDTO cityDTO) {
        String tenantId = TenantContext.getTenantId();
        CityDTO updatedCity = cityService.updateCity(id, cityDTO, tenantId);
        return ResponseEntity.ok(updatedCity);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete city", description = "Soft delete a city")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        cityService.deleteCity(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby cities", description = "Find cities within a specific radius")
    public ResponseEntity<List<CityDTO>> getNearbyCities(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "50") Double radiusKm) {
        String tenantId = TenantContext.getTenantId();
        List<CityDTO> cities = cityService.getNearbyCities(latitude, longitude, radiusKm, tenantId);
        return ResponseEntity.ok(cities);
    }
}
