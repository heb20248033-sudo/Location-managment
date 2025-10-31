package com.location.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "areas", indexes = {
    @Index(name = "idx_area_tenant", columnList = "tenant_id"),
    @Index(name = "idx_area_city", columnList = "city_id"),
    @Index(name = "idx_area_name", columnList = "name"),
    @Index(name = "idx_area_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area extends BaseEntity {

    @NotBlank(message = "Area name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code")
    private String code;

    @NotNull(message = "Area coordinates are required")
    @Column(name = "coordinates", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point coordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    @NotNull(message = "City is required")
    private City city;

    @Column(name = "postal_code")
    private String postalCode;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Location> locations = new ArrayList<>();
}
