package com.location.management.repository;

import com.location.management.entity.Location;
import com.location.management.entity.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT l FROM Location l WHERE l.tenantId = :tenantId AND l.deleted = false")
    Page<Location> findAllByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT l FROM Location l WHERE l.id = :id AND l.tenantId = :tenantId AND l.deleted = false")
    Optional<Location> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT l FROM Location l WHERE l.locationType = :type AND l.tenantId = :tenantId AND l.deleted = false")
    List<Location> findByLocationTypeAndTenantId(@Param("type") LocationType type, @Param("tenantId") String tenantId);

    @Query("SELECT l FROM Location l WHERE l.area.id = :areaId AND l.tenantId = :tenantId AND l.deleted = false")
    List<Location> findByAreaIdAndTenantId(@Param("areaId") Long areaId, @Param("tenantId") String tenantId);

    @Query("SELECT l FROM Location l WHERE l.code = :code AND l.tenantId = :tenantId AND l.deleted = false")
    Optional<Location> findByCodeAndTenantId(@Param("code") String code, @Param("tenantId") String tenantId);

    @Query("SELECT l FROM Location l WHERE l.active = :active AND l.tenantId = :tenantId AND l.deleted = false")
    List<Location> findByActiveAndTenantId(@Param("active") Boolean active, @Param("tenantId") String tenantId);

    @Query(value = "SELECT * FROM locations l WHERE l.tenant_id = :tenantId AND l.deleted = false " +
           "AND ST_DWithin(l.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
           "ORDER BY ST_Distance(l.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography)",
           nativeQuery = true)
    List<Location> findNearbyLocations(@Param("latitude") Double latitude, 
                                        @Param("longitude") Double longitude, 
                                        @Param("radiusMeters") Double radiusMeters,
                                        @Param("tenantId") String tenantId);

    @Query(value = "SELECT l.* FROM locations l " +
           "INNER JOIN areas a ON l.area_id = a.id " +
           "INNER JOIN cities c ON a.city_id = c.id " +
           "WHERE l.tenant_id = :tenantId AND l.location_type = :locationType AND l.deleted = false " +
           "AND ST_DWithin(l.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
           "ORDER BY ST_Distance(l.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) LIMIT :limit",
           nativeQuery = true)
    List<Location> findNearestLocationsByType(@Param("latitude") Double latitude,
                                               @Param("longitude") Double longitude,
                                               @Param("radiusMeters") Double radiusMeters,
                                               @Param("locationType") String locationType,
                                               @Param("tenantId") String tenantId,
                                               @Param("limit") Integer limit);
}
