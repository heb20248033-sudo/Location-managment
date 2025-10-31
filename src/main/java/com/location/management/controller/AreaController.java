package com.location.management.controller;

import com.location.management.config.TenantContext;
import com.location.management.dto.AreaDTO;
import com.location.management.service.AreaService;
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
@RequestMapping("/api/areas")
@RequiredArgsConstructor
@Tag(name = "Area Management", description = "APIs for managing areas")
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    @Operation(summary = "Get all areas", description = "Retrieve all areas with pagination")
    public ResponseEntity<Page<AreaDTO>> getAllAreas(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<AreaDTO> areas = areaService.getAllAreas(tenantId, pageable);
        return ResponseEntity.ok(areas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get area by ID", description = "Retrieve a specific area by its ID")
    public ResponseEntity<AreaDTO> getAreaById(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        AreaDTO area = areaService.getAreaById(id, tenantId);
        return ResponseEntity.ok(area);
    }

    @GetMapping("/city/{cityId}")
    @Operation(summary = "Get areas by city", description = "Retrieve all areas in a specific city")
    public ResponseEntity<List<AreaDTO>> getAreasByCity(@PathVariable Long cityId) {
        String tenantId = TenantContext.getTenantId();
        List<AreaDTO> areas = areaService.getAreasByCity(cityId, tenantId);
        return ResponseEntity.ok(areas);
    }

    @PostMapping
    @Operation(summary = "Create area", description = "Create a new area")
    public ResponseEntity<AreaDTO> createArea(@Valid @RequestBody AreaDTO areaDTO) {
        String tenantId = TenantContext.getTenantId();
        AreaDTO createdArea = areaService.createArea(areaDTO, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdArea);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update area", description = "Update an existing area")
    public ResponseEntity<AreaDTO> updateArea(
            @PathVariable Long id,
            @Valid @RequestBody AreaDTO areaDTO) {
        String tenantId = TenantContext.getTenantId();
        AreaDTO updatedArea = areaService.updateArea(id, areaDTO, tenantId);
        return ResponseEntity.ok(updatedArea);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete area", description = "Soft delete an area")
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        areaService.deleteArea(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby areas", description = "Find areas within a specific radius")
    public ResponseEntity<List<AreaDTO>> getNearbyAreas(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "20") Double radiusKm) {
        String tenantId = TenantContext.getTenantId();
        List<AreaDTO> areas = areaService.getNearbyAreas(latitude, longitude, radiusKm, tenantId);
        return ResponseEntity.ok(areas);
    }
}
