package com.location.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaDTO {
    private Long id;
    
    @NotBlank(message = "Area name is required")
    private String name;
    
    private String code;
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    @NotNull(message = "City ID is required")
    private Long cityId;
    
    private String cityName;
    private String postalCode;
}
