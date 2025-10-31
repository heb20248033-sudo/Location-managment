package com.location.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cities", indexes = {
    @Index(name = "idx_city_tenant", columnList = "tenant_id"),
    @Index(name = "idx_city_name", columnList = "name"),
    @Index(name = "idx_city_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City extends BaseEntity {

    @NotBlank(message = "City name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @NotNull(message = "City coordinates are required")
    @Column(name = "coordinates", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point coordinates;

    @Column(name = "country")
    private String country;

    @Column(name = "population")
    private Long population;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Area> areas = new ArrayList<>();
}
