# Location Management Service

A multi-tenant location management service built with Spring Boot and PostgreSQL with PostGIS for geospatial operations.

## Features

- **Multi-tenant Architecture**: Support for multiple tenants with tenant isolation
- **Location Types**: Manage different location types (warehouses, sorting centers, return centers, etc.)
- **Geospatial Support**: All addresses include geo-coordinates using PostGIS
- **Soft Delete**: No hard deletes - all records are soft deleted for audit purposes
- **Input Validation**: Comprehensive validation on all input data
- **Optimized Queries**: Database queries optimized with proper indexing and batch operations
- **Caching**: Caffeine cache for improved performance
- **RESTful API**: Well-structured REST endpoints with Swagger documentation
- **Scalable**: Designed to handle heavy load with connection pooling and query optimization

## Technology Stack

- Java 17
- Spring Boot 3.1.5
- PostgreSQL with PostGIS
- Hibernate Spatial
- Gradle
- Swagger/OpenAPI 3
- Caffeine Cache
- Lombok

## Prerequisites

- JDK 17 or higher
- PostgreSQL 14+ with PostGIS extension
- Gradle 8.4+ (or use the included Gradle wrapper)

## Database Setup

1. Install PostgreSQL and PostGIS:
```bash
# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib postgis

# macOS
brew install postgresql postgis
```

2. Create database and enable PostGIS:
```sql
CREATE DATABASE locationdb;
\c locationdb;
CREATE EXTENSION postgis;
```

Or use the provided SQL script:
```bash
psql -U postgres -f database-setup.sql
```

## Configuration

Configure the database connection in `src/main/resources/application.yml` or use environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=locationdb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## Running the Application

### Using Gradle Wrapper

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

### Using Docker Compose (Optional)

A Docker Compose file can be created for easy deployment with PostgreSQL:

```yaml
# docker-compose.yml (create this file if needed)
version: '3.8'
services:
  postgres:
    image: postgis/postgis:14-3.3
    environment:
      POSTGRES_DB: locationdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: locationdb
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
    depends_on:
      - postgres

volumes:
  postgres_data:
```

## API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

API documentation is also available at:
```
http://localhost:8080/api-docs
```

## Multi-Tenancy

The application supports multi-tenancy using a tenant ID header. Include the following header in your requests:

```
X-Tenant-ID: your-tenant-id
```

If no tenant ID is provided, the system uses a default tenant.

## API Endpoints

### Cities
- `GET /api/cities` - Get all cities (paginated)
- `GET /api/cities/{id}` - Get city by ID
- `POST /api/cities` - Create a new city
- `PUT /api/cities/{id}` - Update a city
- `DELETE /api/cities/{id}` - Soft delete a city
- `GET /api/cities/nearby?latitude={lat}&longitude={lon}&radiusKm={radius}` - Find nearby cities

### Areas
- `GET /api/areas` - Get all areas (paginated)
- `GET /api/areas/{id}` - Get area by ID
- `GET /api/areas/city/{cityId}` - Get areas by city
- `POST /api/areas` - Create a new area
- `PUT /api/areas/{id}` - Update an area
- `DELETE /api/areas/{id}` - Soft delete an area
- `GET /api/areas/nearby?latitude={lat}&longitude={lon}&radiusKm={radius}` - Find nearby areas

### Locations
- `GET /api/locations` - Get all locations (paginated)
- `GET /api/locations/{id}` - Get location by ID
- `GET /api/locations/type/{type}` - Get locations by type
- `GET /api/locations/area/{areaId}` - Get locations by area
- `GET /api/locations/active` - Get active locations
- `POST /api/locations` - Create a new location
- `PUT /api/locations/{id}` - Update a location
- `DELETE /api/locations/{id}` - Soft delete a location
- `GET /api/locations/nearby?latitude={lat}&longitude={lon}&radiusKm={radius}` - Find nearby locations
- `GET /api/locations/nearest?latitude={lat}&longitude={lon}&type={type}&radiusKm={radius}&limit={limit}` - Find nearest locations by type

## Location Types

The system supports the following location types:
- WAREHOUSE
- SORTING_CENTER
- RETURN_CENTER
- DISTRIBUTION_CENTER
- FULFILLMENT_CENTER
- PICKUP_POINT
- DROP_OFF_POINT
- LOGISTICS_HUB
- CROSS_DOCK
- COLD_STORAGE

## Example Requests

### Create a City
```bash
curl -X POST http://localhost:8080/api/cities \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "name": "New York",
    "code": "NYC",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "country": "USA",
    "population": 8336817
  }'
```

### Create an Area
```bash
curl -X POST http://localhost:8080/api/areas \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "name": "Manhattan",
    "code": "MHT",
    "latitude": 40.7831,
    "longitude": -73.9712,
    "cityId": 1,
    "postalCode": "10001"
  }'
```

### Create a Location
```bash
curl -X POST http://localhost:8080/api/locations \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "name": "Main Warehouse NYC",
    "code": "WH-NYC-001",
    "locationType": "WAREHOUSE",
    "latitude": 40.7589,
    "longitude": -73.9851,
    "areaId": 1,
    "addressLine1": "123 Main St",
    "postalCode": "10001",
    "phoneNumber": "+1-212-555-0100",
    "email": "nyc-warehouse@example.com",
    "capacity": 50000,
    "active": true,
    "description": "Main warehouse in Manhattan"
  }'
```

## Performance Optimizations

1. **Database Indexing**: Proper indexes on tenant_id, location_type, and geospatial columns
2. **Connection Pooling**: HikariCP with optimized pool settings
3. **Batch Operations**: Hibernate batch inserts and updates
4. **Caching**: Caffeine cache for frequently accessed data
5. **Lazy Loading**: JPA lazy loading for related entities
6. **Query Optimization**: Custom queries with proper joins and filters
7. **Geospatial Indexes**: GIST indexes on geometry columns for fast spatial queries

## Best Practices Implemented

- Clean architecture with separation of concerns
- DTO pattern for API contracts
- Service layer for business logic
- Repository pattern for data access
- Global exception handling
- Input validation with Bean Validation
- Soft delete pattern
- Multi-tenancy support
- RESTful API design
- Comprehensive API documentation
- Proper HTTP status codes
- Pagination support

## Testing

Run tests with:
```bash
./gradlew test
```

## Building for Production

```bash
./gradlew build
java -jar build/libs/location-management-1.0.0.jar
```

## License

This project is licensed under the MIT License.
