package com.location.management.service;

import com.location.management.dto.CityDTO;
import com.location.management.entity.City;
import com.location.management.exception.InvalidRequestException;
import com.location.management.exception.ResourceNotFoundException;
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
public class CityService {

    private final CityRepository cityRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Cacheable(value = "cities", key = "#tenantId + '_' + #pageable.pageNumber")
    public Page<CityDTO> getAllCities(String tenantId, Pageable pageable) {
        return cityRepository.findAllByTenantId(tenantId, pageable)
                .map(this::convertToDTO);
    }

    @Cacheable(value = "cities", key = "#tenantId + '_' + #id")
    public CityDTO getCityById(Long id, String tenantId) {
        City city = cityRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        return convertToDTO(city);
    }

    @Transactional
    @CacheEvict(value = "cities", allEntries = true)
    public CityDTO createCity(CityDTO cityDTO, String tenantId) {
        validateCoordinates(cityDTO.getLatitude(), cityDTO.getLongitude());
        
        if (cityDTO.getCode() != null) {
            cityRepository.findByCodeAndTenantId(cityDTO.getCode(), tenantId)
                    .ifPresent(c -> {
                        throw new InvalidRequestException("City with code " + cityDTO.getCode() + " already exists");
                    });
        }

        City city = City.builder()
                .name(cityDTO.getName())
                .code(cityDTO.getCode())
                .coordinates(createPoint(cityDTO.getLatitude(), cityDTO.getLongitude()))
                .country(cityDTO.getCountry())
                .population(cityDTO.getPopulation())
                .build();
        
        city.setTenantId(tenantId);
        City savedCity = cityRepository.save(city);
        return convertToDTO(savedCity);
    }

    @Transactional
    @CacheEvict(value = "cities", allEntries = true)
    public CityDTO updateCity(Long id, CityDTO cityDTO, String tenantId) {
        City city = cityRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));

        validateCoordinates(cityDTO.getLatitude(), cityDTO.getLongitude());

        if (cityDTO.getCode() != null && !cityDTO.getCode().equals(city.getCode())) {
            cityRepository.findByCodeAndTenantId(cityDTO.getCode(), tenantId)
                    .ifPresent(c -> {
                        throw new InvalidRequestException("City with code " + cityDTO.getCode() + " already exists");
                    });
        }

        city.setName(cityDTO.getName());
        city.setCode(cityDTO.getCode());
        city.setCoordinates(createPoint(cityDTO.getLatitude(), cityDTO.getLongitude()));
        city.setCountry(cityDTO.getCountry());
        city.setPopulation(cityDTO.getPopulation());

        City updatedCity = cityRepository.save(city);
        return convertToDTO(updatedCity);
    }

    @Transactional
    @CacheEvict(value = "cities", allEntries = true)
    public void deleteCity(Long id, String tenantId) {
        City city = cityRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        cityRepository.delete(city);
    }

    public List<CityDTO> getNearbyCities(Double latitude, Double longitude, Double radiusKm, String tenantId) {
        validateCoordinates(latitude, longitude);
        Double radiusMeters = radiusKm * 1000;
        return cityRepository.findNearbyCities(latitude, longitude, radiusMeters, tenantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CityDTO convertToDTO(City city) {
        return CityDTO.builder()
                .id(city.getId())
                .name(city.getName())
                .code(city.getCode())
                .latitude(city.getCoordinates().getY())
                .longitude(city.getCoordinates().getX())
                .country(city.getCountry())
                .population(city.getPopulation())
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
