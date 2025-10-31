package com.location.management.controller;

import com.location.management.config.TenantContext;
import com.location.management.dto.LocationDTO;
import com.location.management.entity.LocationType;
import com.location.management.service.LocationService;
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
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "APIs for managing locations")
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    @Operation(summary = "Get all locations", description = "Retrieve all locations with pagination")
    public ResponseEntity<Page<LocationDTO>> getAllLocations(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<LocationDTO> locations = locationService.getAllLocations(tenantId, pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID", description = "Retrieve a specific location by its ID")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        LocationDTO location = locationService.getLocationById(id, tenantId);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get locations by type", description = "Retrieve all locations of a specific type")
    public ResponseEntity<List<LocationDTO>> getLocationsByType(@PathVariable LocationType type) {
        String tenantId = TenantContext.getTenantId();
        List<LocationDTO> locations = locationService.getLocationsByType(type, tenantId);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/area/{areaId}")
    @Operation(summary = "Get locations by area", description = "Retrieve all locations in a specific area")
    public ResponseEntity<List<LocationDTO>> getLocationsByArea(@PathVariable Long areaId) {
        String tenantId = TenantContext.getTenantId();
        List<LocationDTO> locations = locationService.getLocationsByArea(areaId, tenantId);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active locations", description = "Retrieve all active locations")
    public ResponseEntity<List<LocationDTO>> getActiveLocations() {
        String tenantId = TenantContext.getTenantId();
        List<LocationDTO> locations = locationService.getActiveLocations(tenantId);
        return ResponseEntity.ok(locations);
    }

    @PostMapping
    @Operation(summary = "Create location", description = "Create a new location")
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO locationDTO) {
        String tenantId = TenantContext.getTenantId();
        LocationDTO createdLocation = locationService.createLocation(locationDTO, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update location", description = "Update an existing location")
    public ResponseEntity<LocationDTO> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationDTO locationDTO) {
        String tenantId = TenantContext.getTenantId();
        LocationDTO updatedLocation = locationService.updateLocation(id, locationDTO, tenantId);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete location", description = "Soft delete a location")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        locationService.deleteLocation(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby locations", description = "Find locations within a specific radius")
    public ResponseEntity<List<LocationDTO>> getNearbyLocations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm) {
        String tenantId = TenantContext.getTenantId();
        List<LocationDTO> locations = locationService.getNearbyLocations(latitude, longitude, radiusKm, tenantId);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/nearest")
    @Operation(summary = "Find nearest locations by type", description = "Find the nearest locations of a specific type")
    public ResponseEntity<List<LocationDTO>> getNearestLocationsByType(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "50") Double radiusKm,
            @RequestParam LocationType type,
            @RequestParam(defaultValue = "5") Integer limit) {
        String tenantId = TenantContext.getTenantId();
        List<LocationDTO> locations = locationService.getNearestLocationsByType(
                latitude, longitude, radiusKm, type, limit, tenantId);
        return ResponseEntity.ok(locations);
    }
}
