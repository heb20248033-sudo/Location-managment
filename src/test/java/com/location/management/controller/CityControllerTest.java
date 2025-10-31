package com.location.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.location.management.config.TenantContext;
import com.location.management.dto.CityDTO;
import com.location.management.service.CityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CityController.class)
@ActiveProfiles("test")
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CityService cityService;

    private CityDTO testCityDTO;
    private final String tenantId = "test-tenant";

    @BeforeEach
    void setUp() {
        testCityDTO = CityDTO.builder()
                .id(1L)
                .name("New York")
                .code("NYC")
                .latitude(40.7128)
                .longitude(-74.0060)
                .country("USA")
                .population(8336817L)
                .build();
        
        TenantContext.setTenantId(tenantId);
    }

    @Test
    void getAllCities_ShouldReturnPageOfCities() throws Exception {
        Page<CityDTO> cityPage = new PageImpl<>(Arrays.asList(testCityDTO));
        when(cityService.getAllCities(anyString(), any(PageRequest.class)))
                .thenReturn(cityPage);

        mockMvc.perform(get("/api/cities")
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("New York"))
                .andExpect(jsonPath("$.content[0].code").value("NYC"));
    }

    @Test
    void getCityById_ShouldReturnCity() throws Exception {
        when(cityService.getCityById(1L, tenantId))
                .thenReturn(testCityDTO);

        mockMvc.perform(get("/api/cities/1")
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New York"))
                .andExpect(jsonPath("$.code").value("NYC"));
    }

    @Test
    void createCity_WithValidData_ShouldCreateCity() throws Exception {
        when(cityService.createCity(any(CityDTO.class), eq(tenantId)))
                .thenReturn(testCityDTO);

        mockMvc.perform(post("/api/cities")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCityDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New York"));
    }

    @Test
    void createCity_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CityDTO invalidCity = CityDTO.builder().build();

        mockMvc.perform(post("/api/cities")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCity)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCity_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/cities/1")
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isNoContent());
    }
}
