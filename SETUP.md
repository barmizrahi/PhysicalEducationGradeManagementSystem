# Setup Guide - PE Grade Management System

This guide will help you set up the development environment for the Physical Education Grade Management System.

## Prerequisites

Before you begin, ensure you have the following installed:

### Required
- **Java 23** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 18+** and npm ([Download](https://nodejs.org/))
- **Docker Desktop** ([Download](https://www.docker.com/products/docker-desktop/))

### Optional
- **PostgreSQL 14+** (if not using Docker)
- **IntelliJ IDEA** or **VS Code** for development

## Quick Start (Recommended)

The easiest way to get started is using the provided startup scripts:

### Linux/macOS
```bash
chmod +x start-dev.sh
./start-dev.sh
```

### Windows
```cmd
start-dev.bat
```

These scripts will:
1. Start PostgreSQL in Docker
2. Start the Spring Boot backend on port 8080
3. Start the React frontend on port 3000

## Manual Setup

If you prefer to set up each component manually:

### 1. Database Setup

#### Option A: Using Docker (Recommended)
```bash
docker-compose up -d postgres
```

The database will be available at `localhost:5432` with:
- Database: `pe_grades`
- Username: `pe_admin`
- Password: `dev_password_change_in_production`

#### Option B: Local PostgreSQL Installation
```sql
CREATE DATABASE pe_grades;
CREATE USER pe_admin WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE pe_grades TO pe_admin;
```

Then update `src/main/resources/application.properties` with your credentials.

### 2. Backend Setup

```bash
# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will be available at http://localhost:8080

#### Verify Backend
```bash
curl http://localhost:8080/actuator/health
```

You should see: `{"status":"UP"}`

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will be available at http://localhost:3000

## Configuration

### Backend Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/pe_grades
spring.datasource.username=pe_admin
spring.datasource.password=your_password

# JWT Secret (MUST change in production!)
jwt.secret=your-secret-key-change-this-in-production

# CORS (adjust for your frontend URL)
cors.allowed-origins=http://localhost:3000
```

### Frontend Configuration

The frontend is configured via `vite.config.ts`. The default configuration proxies API requests to `http://localhost:8080`.

If your backend runs on a different port, update the proxy configuration:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:YOUR_PORT',
      changeOrigin: true,
    }
  }
}
```

## Verification

### 1. Check Database Connection
```bash
docker-compose exec postgres psql -U pe_admin -d pe_grades -c "\dt"
```

You should see the application tables (after running the backend once).

### 2. Check Backend
Visit http://localhost:8080/actuator/health

### 3. Check Frontend
Visit http://localhost:3000

You should see the application login page.

## Development Tools

### Database Management

#### Using pgAdmin (Docker)
```bash
docker-compose --profile tools up -d pgadmin
```

Access pgAdmin at http://localhost:5050
- Email: `admin@pegrades.local`
- Password: `admin`

#### Using psql (Command Line)
```bash
docker-compose exec postgres psql -U pe_admin -d pe_grades
```

### Hot Reload

- **Backend**: Spring Boot DevTools enables automatic restart on code changes
- **Frontend**: Vite provides instant hot module replacement (HMR)

## Testing

### Backend Tests
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=GradeCalculatorTest

# Run property-based tests only
mvn test -Dtest=*Properties
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Troubleshooting

### Port Already in Use

If port 8080 or 3000 is already in use:

**Backend**: Change port in `application.properties`:
```properties
server.port=8081
```

**Frontend**: Change port in `vite.config.ts`:
```typescript
server: {
  port: 3001,
}
```

### Database Connection Failed

1. Check if PostgreSQL is running:
   ```bash
   docker-compose ps postgres
   ```

2. Check database logs:
   ```bash
   docker-compose logs postgres
   ```

3. Verify credentials in `application.properties`

### Maven Build Fails

1. Verify Java version:
   ```bash
   java -version
   ```
   Should show Java 23 or higher.

2. Clean and rebuild:
   ```bash
   mvn clean install -U
   ```

### Frontend Build Fails

1. Clear node_modules and reinstall:
   ```bash
   cd frontend
   rm -rf node_modules package-lock.json
   npm install
   ```

2. Check Node.js version:
   ```bash
   node -v
   ```
   Should be 18 or higher.

### CORS Errors

If you see CORS errors in the browser console:

1. Verify `cors.allowed-origins` in `application.properties` includes your frontend URL
2. Restart the backend after changing CORS configuration
3. Clear browser cache

## IDE Setup

### IntelliJ IDEA

1. Open the project root directory
2. IDEA should automatically detect it as a Maven project
3. Enable annotation processing: Settings → Build → Compiler → Annotation Processors
4. Set Java SDK to 23: File → Project Structure → Project SDK

### VS Code

Recommended extensions:
- Extension Pack for Java
- Spring Boot Extension Pack
- ESLint
- Prettier
- Vite

## Next Steps

After successful setup:

1. Review the [README.md](README.md) for project overview
2. Check the [API Documentation](#api-documentation) in README
3. Explore the codebase structure
4. Run the test suite to verify everything works
5. Start implementing features!

## Production Deployment

For production deployment:

1. Change JWT secret in `application.properties`
2. Use strong database passwords
3. Enable HTTPS
4. Configure proper CORS origins
5. Set `spring.jpa.hibernate.ddl-auto=validate` (not `update`)
6. Use environment variables for sensitive configuration
7. Build frontend for production: `npm run build`
8. Serve frontend static files through a web server (nginx, Apache)

## Support

If you encounter issues not covered in this guide:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review application logs
3. Search existing issues in the project repository
4. Contact the development team

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
