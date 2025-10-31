package com.location.management.dto;

import com.location.management.entity.LocationType;
import jakarta.validation.constraints.Email;
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
public class LocationDTO {
    private Long id;
    
    @NotBlank(message = "Location name is required")
    private String name;
    
    private String code;
    
    @NotNull(message = "Location type is required")
    private LocationType locationType;
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    @NotNull(message = "Area ID is required")
    private Long areaId;
    
    private String areaName;
    private String cityName;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private Integer capacity;
    private Boolean active;
    private String description;
}
