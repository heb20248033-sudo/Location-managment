package com.location.management.repository;

import com.location.management.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c WHERE c.tenantId = :tenantId AND c.deleted = false")
    Page<City> findAllByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT c FROM City c WHERE c.id = :id AND c.tenantId = :tenantId AND c.deleted = false")
    Optional<City> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM City c WHERE c.name = :name AND c.tenantId = :tenantId AND c.deleted = false")
    Optional<City> findByNameAndTenantId(@Param("name") String name, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM City c WHERE c.code = :code AND c.tenantId = :tenantId AND c.deleted = false")
    Optional<City> findByCodeAndTenantId(@Param("code") String code, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM City c WHERE c.country = :country AND c.tenantId = :tenantId AND c.deleted = false")
    List<City> findByCountryAndTenantId(@Param("country") String country, @Param("tenantId") String tenantId);

    @Query(value = "SELECT * FROM cities c WHERE c.tenant_id = :tenantId AND c.deleted = false " +
           "AND ST_DWithin(c.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
           "ORDER BY ST_Distance(c.coordinates, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography)",
           nativeQuery = true)
    List<City> findNearbyCities(@Param("latitude") Double latitude, 
                                 @Param("longitude") Double longitude, 
                                 @Param("radiusMeters") Double radiusMeters,
                                 @Param("tenantId") String tenantId);
}
