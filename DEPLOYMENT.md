# Deployment Guide

## Prerequisites

### Local Development
- JDK 17 or higher
- PostgreSQL 14+ with PostGIS extension
- Gradle 8.4+ (or use included wrapper)

### Production
- JDK 17 runtime
- PostgreSQL 14+ with PostGIS extension
- Minimum 2GB RAM
- Recommended: Load balancer, monitoring tools

## Database Setup

### PostgreSQL with PostGIS

#### Ubuntu/Debian
```bash
# Install PostgreSQL and PostGIS
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib postgis

# Start PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### macOS
```bash
# Install using Homebrew
brew install postgresql postgis

# Start PostgreSQL
brew services start postgresql
```

#### Docker
```bash
# Run PostgreSQL with PostGIS
docker run --name location-postgres \
  -e POSTGRES_DB=locationdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgis/postgis:14-3.3
```

### Database Initialization

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE locationdb;

# Connect to database
\c locationdb;

# Enable PostGIS extension
CREATE EXTENSION postgis;

# Verify installation
SELECT PostGIS_version();
```

Or use the provided script:
```bash
psql -U postgres -f database-setup.sql
```

## Configuration

### Environment Variables

Set the following environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=locationdb
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
export SERVER_PORT=8080
```

### Application Configuration

Edit `src/main/resources/application.yml` or use environment variables.

#### Production Settings
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
  
  jpa:
    hibernate:
      ddl-auto: validate  # Use validate in production
    show-sql: false

logging:
  level:
    root: INFO
    com.location.management: INFO
```

## Build & Package

### Build JAR
```bash
./gradlew clean build
```

The JAR file will be located at:
```
build/libs/location-management-1.0.0.jar
```

### Run Tests
```bash
./gradlew test
```

### Skip Tests
```bash
./gradlew build -x test
```

## Deployment Options

### 1. Local Deployment

```bash
# Run directly with Gradle
./gradlew bootRun

# Or run the JAR
java -jar build/libs/location-management-1.0.0.jar
```

### 2. Docker Deployment

#### Build Docker Image
```bash
docker build -t location-management:1.0.0 .
```

#### Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Run Docker Container Manually
```bash
docker run -d \
  --name location-app \
  -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=locationdb \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  location-management:1.0.0
```

### 3. Kubernetes Deployment

#### Create ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: location-config
data:
  DB_HOST: "postgres-service"
  DB_PORT: "5432"
  DB_NAME: "locationdb"
  SERVER_PORT: "8080"
```

#### Create Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: location-secret
type: Opaque
stringData:
  DB_USERNAME: "postgres"
  DB_PASSWORD: "your_secure_password"
```

#### Create Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: location-management
spec:
  replicas: 3
  selector:
    matchLabels:
      app: location-management
  template:
    metadata:
      labels:
        app: location-management
    spec:
      containers:
      - name: location-management
        image: location-management:1.0.0
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: location-config
        - secretRef:
            name: location-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

#### Create Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: location-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: location-management
```

#### Deploy to Kubernetes
```bash
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

### 4. Cloud Deployment

#### AWS (Elastic Beanstalk)
```bash
# Install EB CLI
pip install awsebcli

# Initialize EB
eb init -p java-17 location-management

# Create environment
eb create location-env

# Deploy
eb deploy
```

#### AWS (ECS)
```bash
# Build and push to ECR
aws ecr create-repository --repository-name location-management

# Tag and push image
docker tag location-management:1.0.0 <account-id>.dkr.ecr.<region>.amazonaws.com/location-management:1.0.0
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/location-management:1.0.0

# Deploy to ECS (use ECS console or CLI)
```

#### Google Cloud (Cloud Run)
```bash
# Build and push to GCR
gcloud builds submit --tag gcr.io/PROJECT-ID/location-management

# Deploy to Cloud Run
gcloud run deploy location-management \
  --image gcr.io/PROJECT-ID/location-management \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

#### Azure (App Service)
```bash
# Create resource group
az group create --name LocationMgmt --location eastus

# Create App Service plan
az appservice plan create \
  --name LocationPlan \
  --resource-group LocationMgmt \
  --sku B1 \
  --is-linux

# Create web app
az webapp create \
  --resource-group LocationMgmt \
  --plan LocationPlan \
  --name location-management \
  --runtime "JAVA|17-java17"

# Deploy
az webapp deployment source config-zip \
  --resource-group LocationMgmt \
  --name location-management \
  --src build/libs/location-management-1.0.0.jar
```

## Post-Deployment

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### API Documentation
Access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

### Test Endpoints
```bash
# Create a city
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

# Get all cities
curl http://localhost:8080/api/cities \
  -H "X-Tenant-ID: tenant1"
```

## Monitoring

### Logs
```bash
# View logs (Docker)
docker logs -f location-app

# View logs (Kubernetes)
kubectl logs -f deployment/location-management

# View logs (local)
tail -f logs/application.log
```

### Metrics
Enable Spring Boot Actuator endpoints in production:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Database Monitoring
```sql
-- Check connection count
SELECT count(*) FROM pg_stat_activity WHERE datname = 'locationdb';

-- Check slow queries
SELECT query, calls, total_time, mean_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

## Troubleshooting

### Application won't start
- Check database connection
- Verify PostGIS extension is installed
- Check port 8080 is not in use
- Verify Java 17 is installed

### Database connection errors
- Verify database is running
- Check connection credentials
- Verify network connectivity
- Check firewall rules

### Performance issues
- Check database connection pool settings
- Verify indexes are created
- Check slow query log
- Monitor memory usage

### Out of Memory
- Increase JVM heap size: `-Xmx2g`
- Check for memory leaks
- Reduce connection pool size
- Enable garbage collection logging

## Security Checklist

- [ ] Change default database password
- [ ] Use HTTPS in production
- [ ] Enable CORS properly
- [ ] Implement authentication/authorization
- [ ] Use environment variables for secrets
- [ ] Enable SQL injection protection
- [ ] Implement rate limiting
- [ ] Regular security updates
- [ ] Database backups configured
- [ ] Monitor access logs

## Backup & Recovery

### Database Backup
```bash
# Backup
pg_dump -U postgres locationdb > backup.sql

# Backup with PostGIS
pg_dump -U postgres -F c locationdb > backup.dump

# Restore
pg_restore -U postgres -d locationdb backup.dump
```

### Automated Backups
```bash
# Cron job for daily backups
0 2 * * * pg_dump -U postgres locationdb > /backups/location-$(date +\%Y\%m\%d).sql
```

## Scaling

### Horizontal Scaling
- Deploy multiple application instances
- Use load balancer (Nginx, AWS ALB, etc.)
- Ensure database can handle connections
- Consider read replicas for read-heavy workloads

### Vertical Scaling
- Increase JVM heap size
- Increase database resources
- Increase connection pool size
- Optimize queries further

### Database Scaling
- Read replicas for read operations
- Connection pooling optimization
- Table partitioning by tenant_id
- Caching layer (Redis)
