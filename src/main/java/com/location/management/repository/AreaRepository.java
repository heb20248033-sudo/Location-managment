package com.location.management.repository;

import com.location.management.entity.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {

    @Query("SELECT a FROM Area a WHERE a.tenantId = :tenantId AND a.deleted = false")
    Page<Area> findAllByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT a FROM Area a WHERE a.id = :id AND a.tenantId = :tenantId AND a.deleted = false")
    Optional<Area> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT a FROM Area a WHERE a.city.id = :cityId AND a.tenantId = :tenantId AND a.deleted = false")
    List<Area> findByCityIdAndTenantId(@Param("cityId") Long cityId, @Param("tenantId") String tenantId);

    @Query("SELECT a FROM Area a WHERE a.code = :code AND a.tenantId = :tenantId AND a.deleted = false")
    Optional<Area> findByCodeAndTenantId(@Param("code") String code, @Param("tenantId") String tenantId);

    @Query(value = "SELECT * FROM areas a WHERE a.tenant_id = :tenantId AND a.deleted = false " +
           "AND ST_DWithin(a.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
           "ORDER BY ST_Distance(a.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography)",
           nativeQuery = true)
    List<Area> findNearbyAreas(@Param("latitude") Double latitude, 
                                @Param("longitude") Double longitude, 
                                @Param("radiusMeters") Double radiusMeters,
                                @Param("tenantId") String tenantId);
}
