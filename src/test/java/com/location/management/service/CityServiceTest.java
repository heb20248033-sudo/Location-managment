package com.location.management.service;

import com.location.management.dto.CityDTO;
import com.location.management.entity.City;
import com.location.management.exception.InvalidRequestException;
import com.location.management.exception.ResourceNotFoundException;
import com.location.management.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private City testCity;
    private CityDTO testCityDTO;
    private final String tenantId = "test-tenant";

    @BeforeEach
    void setUp() {
        Point coordinates = geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128));
        
        testCity = City.builder()
                .name("New York")
                .code("NYC")
                .coordinates(coordinates)
                .country("USA")
                .population(8336817L)
                .build();
        testCity.setId(1L);
        testCity.setTenantId(tenantId);

        testCityDTO = CityDTO.builder()
                .name("New York")
                .code("NYC")
                .latitude(40.7128)
                .longitude(-74.0060)
                .country("USA")
                .population(8336817L)
                .build();
    }

    @Test
    void getAllCities_ShouldReturnPageOfCities() {
        Page<City> cityPage = new PageImpl<>(Arrays.asList(testCity));
        when(cityRepository.findAllByTenantId(eq(tenantId), any(PageRequest.class)))
                .thenReturn(cityPage);

        Page<CityDTO> result = cityService.getAllCities(tenantId, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("New York", result.getContent().get(0).getName());
        verify(cityRepository).findAllByTenantId(eq(tenantId), any(PageRequest.class));
    }

    @Test
    void getCityById_WhenCityExists_ShouldReturnCity() {
        when(cityRepository.findByIdAndTenantId(1L, tenantId))
                .thenReturn(Optional.of(testCity));

        CityDTO result = cityService.getCityById(1L, tenantId);

        assertNotNull(result);
        assertEquals("New York", result.getName());
        assertEquals("NYC", result.getCode());
        verify(cityRepository).findByIdAndTenantId(1L, tenantId);
    }

    @Test
    void getCityById_WhenCityDoesNotExist_ShouldThrowException() {
        when(cityRepository.findByIdAndTenantId(1L, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> cityService.getCityById(1L, tenantId));
        verify(cityRepository).findByIdAndTenantId(1L, tenantId);
    }

    @Test
    void createCity_WithValidData_ShouldCreateCity() {
        when(cityRepository.findByCodeAndTenantId(anyString(), eq(tenantId)))
                .thenReturn(Optional.empty());
        when(cityRepository.save(any(City.class))).thenReturn(testCity);

        CityDTO result = cityService.createCity(testCityDTO, tenantId);

        assertNotNull(result);
        assertEquals("New York", result.getName());
        verify(cityRepository).save(any(City.class));
    }

    @Test
    void createCity_WithInvalidLatitude_ShouldThrowException() {
        testCityDTO.setLatitude(100.0);

        assertThrows(InvalidRequestException.class, 
                () -> cityService.createCity(testCityDTO, tenantId));
    }

    @Test
    void createCity_WithInvalidLongitude_ShouldThrowException() {
        testCityDTO.setLongitude(200.0);

        assertThrows(InvalidRequestException.class, 
                () -> cityService.createCity(testCityDTO, tenantId));
    }

    @Test
    void deleteCity_WhenCityExists_ShouldDeleteCity() {
        when(cityRepository.findByIdAndTenantId(1L, tenantId))
                .thenReturn(Optional.of(testCity));

        cityService.deleteCity(1L, tenantId);

        verify(cityRepository).delete(testCity);
    }

    @Test
    void deleteCity_WhenCityDoesNotExist_ShouldThrowException() {
        when(cityRepository.findByIdAndTenantId(1L, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> cityService.deleteCity(1L, tenantId));
    }
}
