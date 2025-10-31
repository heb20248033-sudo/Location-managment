package com.location.management.service;

import com.location.management.dto.AreaDTO;
import com.location.management.entity.Area;
import com.location.management.entity.City;
import com.location.management.exception.InvalidRequestException;
import com.location.management.exception.ResourceNotFoundException;
import com.location.management.repository.AreaRepository;
import com.location.management.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaService {

    private final AreaRepository areaRepository;
    private final CityRepository cityRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Cacheable(value = "areas", key = "#tenantId + '_' + #pageable.pageNumber")
    public Page<AreaDTO> getAllAreas(String tenantId, Pageable pageable) {
        return areaRepository.findAllByTenantId(tenantId, pageable)
                .map(this::convertToDTO);
    }

    @Cacheable(value = "areas", key = "#tenantId + '_' + #id")
    public AreaDTO getAreaById(Long id, String tenantId) {
        Area area = areaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + id));
        return convertToDTO(area);
    }

    public List<AreaDTO> getAreasByCity(Long cityId, String tenantId) {
        return areaRepository.findByCityIdAndTenantId(cityId, tenantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "areas", allEntries = true)
    public AreaDTO createArea(AreaDTO areaDTO, String tenantId) {
        validateCoordinates(areaDTO.getLatitude(), areaDTO.getLongitude());

        City city = cityRepository.findByIdAndTenantId(areaDTO.getCityId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + areaDTO.getCityId()));

        if (areaDTO.getCode() != null) {
            areaRepository.findByCodeAndTenantId(areaDTO.getCode(), tenantId)
                    .ifPresent(a -> {
                        throw new InvalidRequestException("Area with code " + areaDTO.getCode() + " already exists");
                    });
        }

        Area area = Area.builder()
                .name(areaDTO.getName())
                .code(areaDTO.getCode())
                .coordinates(createPoint(areaDTO.getLatitude(), areaDTO.getLongitude()))
                .city(city)
                .postalCode(areaDTO.getPostalCode())
                .build();

        area.setTenantId(tenantId);
        Area savedArea = areaRepository.save(area);
        return convertToDTO(savedArea);
    }

    @Transactional
    @CacheEvict(value = "areas", allEntries = true)
    public AreaDTO updateArea(Long id, AreaDTO areaDTO, String tenantId) {
        Area area = areaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + id));

        validateCoordinates(areaDTO.getLatitude(), areaDTO.getLongitude());

        City city = cityRepository.findByIdAndTenantId(areaDTO.getCityId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + areaDTO.getCityId()));

        if (areaDTO.getCode() != null && !areaDTO.getCode().equals(area.getCode())) {
            areaRepository.findByCodeAndTenantId(areaDTO.getCode(), tenantId)
                    .ifPresent(a -> {
                        throw new InvalidRequestException("Area with code " + areaDTO.getCode() + " already exists");
                    });
        }

        area.setName(areaDTO.getName());
        area.setCode(areaDTO.getCode());
        area.setCoordinates(createPoint(areaDTO.getLatitude(), areaDTO.getLongitude()));
        area.setCity(city);
        area.setPostalCode(areaDTO.getPostalCode());

        Area updatedArea = areaRepository.save(area);
        return convertToDTO(updatedArea);
    }

    @Transactional
    @CacheEvict(value = "areas", allEntries = true)
    public void deleteArea(Long id, String tenantId) {
        Area area = areaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + id));
        areaRepository.delete(area);
    }

    public List<AreaDTO> getNearbyAreas(Double latitude, Double longitude, Double radiusKm, String tenantId) {
        validateCoordinates(latitude, longitude);
        Double radiusMeters = radiusKm * 1000;
        return areaRepository.findNearbyAreas(latitude, longitude, radiusMeters, tenantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AreaDTO convertToDTO(Area area) {
        return AreaDTO.builder()
                .id(area.getId())
                .name(area.getName())
                .code(area.getCode())
                .latitude(area.getCoordinates().getY())
                .longitude(area.getCoordinates().getX())
                .cityId(area.getCity().getId())
                .cityName(area.getCity().getName())
                .postalCode(area.getPostalCode())
                .build();
    }

    private Point createPoint(Double latitude, Double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new InvalidRequestException("Latitude and longitude are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new InvalidRequestException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new InvalidRequestException("Longitude must be between -180 and 180");
        }
    }
}
