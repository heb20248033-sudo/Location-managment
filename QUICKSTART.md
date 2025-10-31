# Quick Start Guide

Get the Location Management Service up and running in minutes!

## 🚀 Fast Track (Using Docker)

### Prerequisites
- Docker and Docker Compose installed

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/heb20248033-sudo/Location-managment.git
cd Location-managment
```

2. **Start the services**
```bash
docker-compose up -d
```

3. **Verify it's running**
```bash
curl http://localhost:8080/swagger-ui.html
```

4. **Access Swagger UI**
Open your browser: `http://localhost:8080/swagger-ui.html`

That's it! The application and PostgreSQL with PostGIS are now running.

## 🛠️ Manual Setup

### Prerequisites
- JDK 17+
- PostgreSQL 14+ with PostGIS extension
- Gradle 8.4+ (or use the included wrapper)

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/heb20248033-sudo/Location-managment.git
cd Location-managment
```

2. **Setup PostgreSQL**
```bash
# Start PostgreSQL
sudo systemctl start postgresql

# Create database and enable PostGIS
psql -U postgres -f database-setup.sql
```

3. **Configure the application**
```bash
# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=locationdb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

4. **Build and run**
```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

5. **Access the application**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API docs: `http://localhost:8080/api-docs`

## 📝 First API Calls

### 1. Create a City
```bash
curl -X POST http://localhost:8080/api/cities \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: demo" \
  -d '{
    "name": "New York",
    "code": "NYC",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "country": "USA",
    "population": 8336817
  }'
```

### 2. Get All Cities
```bash
curl http://localhost:8080/api/cities \
  -H "X-Tenant-ID: demo"
```

### 3. Create an Area
```bash
curl -X POST http://localhost:8080/api/areas \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: demo" \
  -d '{
    "name": "Manhattan",
    "code": "MHT",
    "latitude": 40.7831,
    "longitude": -73.9712,
    "cityId": 1,
    "postalCode": "10001"
  }'
```

### 4. Create a Location
```bash
curl -X POST http://localhost:8080/api/locations \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: demo" \
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

### 5. Find Nearby Locations
```bash
curl "http://localhost:8080/api/locations/nearby?latitude=40.7589&longitude=-73.9851&radiusKm=10" \
  -H "X-Tenant-ID: demo"
```

### 6. Find Nearest Warehouses
```bash
curl "http://localhost:8080/api/locations/nearest?latitude=40.7589&longitude=-73.9851&type=WAREHOUSE&radiusKm=50&limit=5" \
  -H "X-Tenant-ID: demo"
```

## 🎯 Key Features to Try

### Multi-Tenancy
Use different tenant IDs to see data isolation:
```bash
# Tenant 1
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/cities

# Tenant 2 (will show different data)
curl -H "X-Tenant-ID: tenant2" http://localhost:8080/api/cities
```

### Geospatial Queries
Find all locations within 20km of Times Square:
```bash
curl "http://localhost:8080/api/locations/nearby?latitude=40.758&longitude=-73.985&radiusKm=20" \
  -H "X-Tenant-ID: demo"
```

### Pagination
Get paginated results:
```bash
curl "http://localhost:8080/api/cities?page=0&size=10" \
  -H "X-Tenant-ID: demo"
```

### Soft Delete
Delete a city (soft delete - data preserved):
```bash
curl -X DELETE http://localhost:8080/api/cities/1 \
  -H "X-Tenant-ID: demo"
```

### Location Types
Get all warehouses:
```bash
curl http://localhost:8080/api/locations/type/WAREHOUSE \
  -H "X-Tenant-ID: demo"
```

Get all active locations:
```bash
curl http://localhost:8080/api/locations/active \
  -H "X-Tenant-ID: demo"
```

## 🧪 Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test
./gradlew test --tests CityServiceTest
```

## 🐛 Troubleshooting

### Port 8080 already in use
```bash
# Change port
export SERVER_PORT=8081
./gradlew bootRun
```

### Database connection refused
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check connection details
psql -U postgres -d locationdb -c "SELECT 1;"
```

### PostGIS not found
```bash
# Install PostGIS
sudo apt-get install postgis

# Enable in database
psql -U postgres -d locationdb -c "CREATE EXTENSION postgis;"
```

### Build fails
```bash
# Clean and rebuild
./gradlew clean build

# Skip tests
./gradlew build -x test
```

## 📚 Next Steps

1. **Explore API Documentation**: Visit `http://localhost:8080/swagger-ui.html`
2. **Read Architecture Guide**: See `ARCHITECTURE.md`
3. **Check Deployment Options**: See `DEPLOYMENT.md`
4. **Review API Examples**: See `API-DOCUMENTATION.md`
5. **Explore Features**: See `FEATURES.md`

## 💡 Tips

- Use Swagger UI for interactive API testing
- Always include `X-Tenant-ID` header
- Coordinates must be in valid ranges (lat: -90 to 90, lon: -180 to 180)
- All deletes are soft deletes - data is never actually removed
- Use pagination for large result sets
- Geospatial queries use kilometers for radius

## 🤝 Need Help?

- Check the logs: `docker-compose logs -f` or `./gradlew bootRun`
- Review error messages in API responses
- Consult the comprehensive documentation files
- Check database connectivity

## 🎉 You're Ready!

The Location Management Service is now running and ready to handle your location management needs with multi-tenancy, geospatial capabilities, and optimized performance!
