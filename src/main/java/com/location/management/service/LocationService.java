package com.location.management.service;

import com.location.management.dto.LocationDTO;
import com.location.management.entity.Area;
import com.location.management.entity.Location;
import com.location.management.entity.LocationType;
import com.location.management.exception.InvalidRequestException;
import com.location.management.exception.ResourceNotFoundException;
import com.location.management.repository.AreaRepository;
import com.location.management.repository.LocationRepository;
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
public class LocationService {

    private final LocationRepository locationRepository;
    private final AreaRepository areaRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Cacheable(value = "locations", key = "#tenantId + '_' + #pageable.pageNumber")
    public Page<LocationDTO> getAllLocations(String tenantId, Pageable pageable) {
        return locationRepository.findAllByTenantId(tenantId, pageable)
                .map(this::convertToDTO);
    }

    @Cacheable(value = "locations", key = "#tenantId + '_' + #id")
    public LocationDTO getLocationById(Long id, String tenantId) {
        Location location = locationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return convertToDTO(location);
    }

    public List<LocationDTO> getLocationsByType(LocationType type, String tenantId) {
        return locationRepository.findByLocationTypeAndTenantId(type, tenantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LocationDTO> getLocationsByArea(Long areaId, String tenantId) {
        return locationRepository.findByAreaIdAndTenantId(areaId, tenantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LocationDTO> getActiveLocations(String tenantId) {
        return locationRepository.findByActiveAndTenantId(true, tenantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public LocationDTO createLocation(LocationDTO locationDTO, String tenantId) {
        validateCoordinates(locationDTO.getLatitude(), locationDTO.getLongitude());

        Area area = areaRepository.findByIdAndTenantId(locationDTO.getAreaId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + locationDTO.getAreaId()));

        if (locationDTO.getCode() != null) {
            locationRepository.findByCodeAndTenantId(locationDTO.getCode(), tenantId)
                    .ifPresent(l -> {
                        throw new InvalidRequestException("Location with code " + locationDTO.getCode() + " already exists");
                    });
        }

        Location location = Location.builder()
                .name(locationDTO.getName())
                .code(locationDTO.getCode())
                .locationType(locationDTO.getLocationType())
                .coordinates(createPoint(locationDTO.getLatitude(), locationDTO.getLongitude()))
                .area(area)
                .addressLine1(locationDTO.getAddressLine1())
                .addressLine2(locationDTO.getAddressLine2())
                .postalCode(locationDTO.getPostalCode())
                .phoneNumber(locationDTO.getPhoneNumber())
                .email(locationDTO.getEmail())
                .capacity(locationDTO.getCapacity())
                .active(locationDTO.getActive() != null ? locationDTO.getActive() : true)
                .description(locationDTO.getDescription())
                .build();

        location.setTenantId(tenantId);
        Location savedLocation = locationRepository.save(location);
        return convertToDTO(savedLocation);
    }

    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public LocationDTO updateLocation(Long id, LocationDTO locationDTO, String tenantId) {
        Location location = locationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        validateCoordinates(locationDTO.getLatitude(), locationDTO.getLongitude());

        Area area = areaRepository.findByIdAndTenantId(locationDTO.getAreaId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + locationDTO.getAreaId()));

        if (locationDTO.getCode() != null && !locationDTO.getCode().equals(location.getCode())) {
            locationRepository.findByCodeAndTenantId(locationDTO.getCode(), tenantId)
                    .ifPresent(l -> {
                        throw new InvalidRequestException("Location with code " + locationDTO.getCode() + " already exists");
                    });
        }

        location.setName(locationDTO.getName());
        location.setCode(locationDTO.getCode());
        location.setLocationType(locationDTO.getLocationType());
        location.setCoordinates(createPoint(locationDTO.getLatitude(), locationDTO.getLongitude()));
        location.setArea(area);
        location.setAddressLine1(locationDTO.getAddressLine1());
        location.setAddressLine2(locationDTO.getAddressLine2());
        location.setPostalCode(locationDTO.getPostalCode());
        location.setPhoneNumber(locationDTO.getPhoneNumber());
        location.setEmail(locationDTO.getEmail());
        location.setCapacity(locationDTO.getCapacity());
        location.setActive(locationDTO.getActive() != null ? locationDTO.getActive() : true);
        location.setDescription(locationDTO.getDescription());

        Location updatedLocation = locationRepository.save(location);
        return convertToDTO(updatedLocation);
    }

    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public void deleteLocation(Long id, String tenantId) {
        Location location = locationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        locationRepository.delete(location);
    }

    public List<LocationDTO> getNearbyLocations(Double latitude, Double longitude, Double radiusKm, String tenantId) {
        validateCoordinates(latitude, longitude);
        Double radiusMeters = radiusKm * 1000;
        return locationRepository.findNearbyLocations(latitude, longitude, radiusMeters, tenantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LocationDTO> getNearestLocationsByType(Double latitude, Double longitude, 
                                                        Double radiusKm, LocationType type, 
                                                        Integer limit, String tenantId) {
        validateCoordinates(latitude, longitude);
        Double radiusMeters = radiusKm * 1000;
        return locationRepository.findNearestLocationsByType(
                latitude, longitude, radiusMeters, type.name(), tenantId, limit)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private LocationDTO convertToDTO(Location location) {
        return LocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .code(location.getCode())
                .locationType(location.getLocationType())
                .latitude(location.getCoordinates().getY())
                .longitude(location.getCoordinates().getX())
                .areaId(location.getArea().getId())
                .areaName(location.getArea().getName())
                .cityName(location.getArea().getCity().getName())
                .addressLine1(location.getAddressLine1())
                .addressLine2(location.getAddressLine2())
                .postalCode(location.getPostalCode())
                .phoneNumber(location.getPhoneNumber())
                .email(location.getEmail())
                .capacity(location.getCapacity())
                .active(location.getActive())
                .description(location.getDescription())
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
