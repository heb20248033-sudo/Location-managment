package com.location.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "locations", indexes = {
    @Index(name = "idx_location_tenant", columnList = "tenant_id"),
    @Index(name = "idx_location_area", columnList = "area_id"),
    @Index(name = "idx_location_type", columnList = "location_type"),
    @Index(name = "idx_location_deleted", columnList = "deleted"),
    @Index(name = "idx_location_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location extends BaseEntity {

    @NotBlank(message = "Location name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @NotNull(message = "Location type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private LocationType locationType;

    @NotNull(message = "Location coordinates are required")
    @Column(name = "coordinates", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point coordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    @NotNull(message = "Area is required")
    private Area area;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "description", length = 1000)
    private String description;
}
