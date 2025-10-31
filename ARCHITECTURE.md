# Architecture Documentation

## Overview
The Location Management Service is a multi-tenant Spring Boot application designed to manage different types of locations (warehouses, sorting centers, return centers, etc.) with geospatial capabilities using PostgreSQL with PostGIS.

## Architecture Layers

### 1. Controller Layer
- **Purpose**: Handle HTTP requests and responses
- **Key Features**:
  - RESTful endpoint definitions
  - Request validation
  - Response formatting
  - Swagger/OpenAPI documentation
  - Multi-tenant header extraction

**Controllers:**
- `CityController` - Manages city-related endpoints
- `AreaController` - Manages area-related endpoints
- `LocationController` - Manages location-related endpoints

### 2. Service Layer
- **Purpose**: Business logic and transaction management
- **Key Features**:
  - Business rule validation
  - Coordinate validation
  - DTO to Entity conversion
  - Caching logic
  - Transaction boundaries

**Services:**
- `CityService` - City business logic
- `AreaService` - Area business logic
- `LocationService` - Location business logic

### 3. Repository Layer
- **Purpose**: Data access and persistence
- **Key Features**:
  - Custom JPA queries
  - Optimized geospatial queries
  - Tenant-aware filtering
  - Soft delete support

**Repositories:**
- `CityRepository` - City data access
- `AreaRepository` - Area data access
- `LocationRepository` - Location data access

### 4. Entity Layer
- **Purpose**: Domain model representation
- **Key Features**:
  - JPA annotations
  - Soft delete support
  - Audit fields (created_at, updated_at)
  - Geospatial support (Point coordinates)
  - Multi-tenant support

**Entities:**
- `BaseEntity` - Abstract base with common fields
- `City` - City entity
- `Area` - Area entity (belongs to City)
- `Location` - Location entity (belongs to Area)
- `LocationType` - Enum for location types

### 5. DTO Layer
- **Purpose**: Data transfer objects for API contracts
- **Key Features**:
  - Input validation annotations
  - Decoupling from domain model
  - Clean API contracts

**DTOs:**
- `CityDTO`
- `AreaDTO`
- `LocationDTO`

### 6. Configuration Layer
- **Purpose**: Application configuration
- **Components**:
  - `TenantContext` - ThreadLocal tenant storage
  - `TenantInterceptor` - Extract tenant from headers
  - `WebConfig` - MVC configuration

### 7. Exception Handling
- **Purpose**: Global exception handling
- **Components**:
  - `GlobalExceptionHandler` - Centralized exception handling
  - `ResourceNotFoundException` - Entity not found
  - `InvalidRequestException` - Validation errors

## Multi-Tenancy Design

The application uses a **discriminator-based multi-tenancy** approach:

1. **Tenant Identification**: Via HTTP header `X-Tenant-ID`
2. **Tenant Context**: Stored in ThreadLocal via `TenantContext`
3. **Data Isolation**: Every entity includes `tenant_id` column
4. **Query Filtering**: All queries automatically filter by tenant_id

```
Request → TenantInterceptor → TenantContext → Service → Repository (filtered by tenant)
```

## Geospatial Architecture

### PostGIS Integration
- **Extension**: PostGIS for PostgreSQL
- **Data Type**: `geometry(Point, 4326)` using WGS 84 coordinate system
- **Spatial Queries**: 
  - `ST_DWithin` - Find within radius
  - `ST_Distance` - Calculate distance
  - `ST_MakePoint` - Create point geometry

### Hibernate Spatial
- Integrates PostGIS with JPA/Hibernate
- Maps PostGIS geometry types to Java `Point` objects
- Supports spatial queries in JPQL and native SQL

## Soft Delete Pattern

All entities support soft delete:
1. `deleted` boolean flag (default: false)
2. `deleted_at` timestamp
3. Hibernate's `@SQLDelete` annotation
4. Hibernate's `@Where` clause filters deleted records

**Delete Flow:**
```
Controller.delete() → Service.delete() → Repository.delete() → SQL UPDATE (sets deleted=true)
```

## Caching Strategy

**Cache Provider**: Caffeine Cache
**Cache Configuration**:
- Maximum size: 1000 entries
- TTL: 600 seconds (10 minutes)

**Cached Operations:**
- `getAllCities/Areas/Locations` - Cached by tenant + page
- `getCityById/AreaById/LocationById` - Cached by tenant + id

**Cache Eviction:**
- Automatic on create/update/delete operations
- Uses `@CacheEvict(allEntries = true)`

## Database Optimization

### Indexing Strategy
1. **Tenant Indexes**: `idx_*_tenant` on `tenant_id`
2. **Type Indexes**: `idx_location_type` on `location_type`
3. **Status Indexes**: `idx_*_deleted`, `idx_location_active`
4. **Relationship Indexes**: Foreign key indexes
5. **Spatial Indexes**: GIST indexes on geometry columns

### Query Optimization
1. **Pagination**: All list operations support pagination
2. **Lazy Loading**: Relationships use LAZY fetch type
3. **Batch Operations**: Hibernate batch inserts/updates configured
4. **Connection Pooling**: HikariCP with optimized settings
   - Max pool size: 20
   - Min idle: 5
   - Connection timeout: 30s

### Sample Optimized Query
```sql
SELECT * FROM locations l 
WHERE l.tenant_id = :tenantId 
  AND l.deleted = false 
  AND ST_DWithin(
    l.coordinates, 
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 
    :radiusMeters
  )
ORDER BY ST_Distance(
  l.coordinates, 
  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
)
```

## Data Flow

### Create Location Example
```
1. Client → POST /api/locations (with X-Tenant-ID header)
2. TenantInterceptor → Extract tenant ID → Store in TenantContext
3. LocationController → Validate request body
4. LocationService → 
   - Validate coordinates
   - Check if area exists
   - Check for duplicate code
   - Convert DTO to Entity
   - Set tenant ID
   - Create Point geometry
5. LocationRepository → Save to database
6. Response → Return LocationDTO
```

## Scalability Considerations

### Database Level
1. **Read Replicas**: Can configure read-only replicas
2. **Partitioning**: Table partitioning by tenant_id (future)
3. **Indexing**: Comprehensive index strategy
4. **Connection Pooling**: Properly sized connection pool

### Application Level
1. **Horizontal Scaling**: Stateless design allows multiple instances
2. **Caching**: Reduces database load
3. **Async Processing**: Can add message queues for heavy operations
4. **Rate Limiting**: Can add rate limiting per tenant

### Load Handling
1. **Pagination**: Prevents large data transfers
2. **Lazy Loading**: Loads related data on-demand
3. **Batch Processing**: Hibernate batch operations
4. **Query Optimization**: Indexed and optimized queries

## Security Features

1. **Input Validation**: Bean Validation on all DTOs
2. **SQL Injection Prevention**: JPA/Hibernate parameterized queries
3. **Tenant Isolation**: Automatic tenant filtering
4. **Soft Delete**: Audit trail preservation
5. **No Hard Delete**: Prevents accidental data loss

## Monitoring & Observability

### Logging
- **Framework**: Logback
- **Levels**: Configurable per package
- **SQL Logging**: Hibernate SQL logging for debugging

### Metrics (Future)
- Spring Boot Actuator endpoints
- Database connection pool metrics
- Cache hit/miss ratios
- Request latency metrics

## Future Enhancements

1. **Authentication & Authorization**: JWT-based auth, role-based access
2. **Audit Logging**: Track who created/modified records
3. **Async Processing**: Message queue for heavy operations
4. **Event Sourcing**: Track all changes to entities
5. **API Versioning**: Support multiple API versions
6. **Rate Limiting**: Per-tenant rate limiting
7. **Advanced Spatial Queries**: Polygon boundaries, routing
8. **Time-series Data**: Track location usage over time
9. **Analytics**: Location performance metrics
10. **Multi-region Support**: Geographic data distribution
