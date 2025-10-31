# API Documentation

## Base URL
```
http://localhost:8080
```

## Headers
All requests should include the tenant ID header:
```
X-Tenant-ID: your-tenant-id
```

## City Endpoints

### Get All Cities
```http
GET /api/cities?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "New York",
      "code": "NYC",
      "latitude": 40.7128,
      "longitude": -74.0060,
      "country": "USA",
      "population": 8336817
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 1,
  "totalElements": 1
}
```

### Get City by ID
```http
GET /api/cities/{id}
```

**Response:**
```json
{
  "id": 1,
  "name": "New York",
  "code": "NYC",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "country": "USA",
  "population": 8336817
}
```

### Create City
```http
POST /api/cities
Content-Type: application/json

{
  "name": "New York",
  "code": "NYC",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "country": "USA",
  "population": 8336817
}
```

**Response:** 201 Created
```json
{
  "id": 1,
  "name": "New York",
  "code": "NYC",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "country": "USA",
  "population": 8336817
}
```

### Update City
```http
PUT /api/cities/{id}
Content-Type: application/json

{
  "name": "New York City",
  "code": "NYC",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "country": "USA",
  "population": 8500000
}
```

**Response:** 200 OK

### Delete City (Soft Delete)
```http
DELETE /api/cities/{id}
```

**Response:** 204 No Content

### Find Nearby Cities
```http
GET /api/cities/nearby?latitude=40.7128&longitude=-74.0060&radiusKm=50
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "New York",
    "code": "NYC",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "country": "USA",
    "population": 8336817
  }
]
```

## Area Endpoints

### Get All Areas
```http
GET /api/areas?page=0&size=10
```

### Get Area by ID
```http
GET /api/areas/{id}
```

### Get Areas by City
```http
GET /api/areas/city/{cityId}
```

### Create Area
```http
POST /api/areas
Content-Type: application/json

{
  "name": "Manhattan",
  "code": "MHT",
  "latitude": 40.7831,
  "longitude": -73.9712,
  "cityId": 1,
  "postalCode": "10001"
}
```

### Update Area
```http
PUT /api/areas/{id}
Content-Type: application/json
```

### Delete Area (Soft Delete)
```http
DELETE /api/areas/{id}
```

### Find Nearby Areas
```http
GET /api/areas/nearby?latitude=40.7831&longitude=-73.9712&radiusKm=20
```

## Location Endpoints

### Get All Locations
```http
GET /api/locations?page=0&size=10
```

### Get Location by ID
```http
GET /api/locations/{id}
```

### Get Locations by Type
```http
GET /api/locations/type/{type}
```

Available types:
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

### Get Locations by Area
```http
GET /api/locations/area/{areaId}
```

### Get Active Locations
```http
GET /api/locations/active
```

### Create Location
```http
POST /api/locations
Content-Type: application/json

{
  "name": "Main Warehouse NYC",
  "code": "WH-NYC-001",
  "locationType": "WAREHOUSE",
  "latitude": 40.7589,
  "longitude": -73.9851,
  "areaId": 1,
  "addressLine1": "123 Main St",
  "addressLine2": "Suite 100",
  "postalCode": "10001",
  "phoneNumber": "+1-212-555-0100",
  "email": "nyc-warehouse@example.com",
  "capacity": 50000,
  "active": true,
  "description": "Main warehouse in Manhattan"
}
```

### Update Location
```http
PUT /api/locations/{id}
Content-Type: application/json
```

### Delete Location (Soft Delete)
```http
DELETE /api/locations/{id}
```

### Find Nearby Locations
```http
GET /api/locations/nearby?latitude=40.7589&longitude=-73.9851&radiusKm=10
```

### Find Nearest Locations by Type
```http
GET /api/locations/nearest?latitude=40.7589&longitude=-73.9851&radiusKm=50&type=WAREHOUSE&limit=5
```

## Error Responses

### 400 Bad Request
```json
{
  "status": 400,
  "errors": {
    "name": "City name is required",
    "latitude": "Latitude is required"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "City not found with id: 1",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred: ...",
  "timestamp": "2024-01-01T12:00:00"
}
```

## Swagger UI
Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```
