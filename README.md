# Physical Education Grade Management System

A web application designed to streamline grade management for high school PE teachers in Israel. The system eliminates double data entry by providing a digital workflow from student import through grade calculation to Ministry of Education-compatible export.

## Features

### Core Functionality
- **Student Management**: Import student rosters from Excel files with duplicate detection
- **Test Configuration**: Create tests with customizable grading formulas (RATIO and PENALTY methods)
- **Mobile Grade Entry**: Fast, touch-optimized interface for entering grades during class
- **Automated Calculation**: Automatic grade calculation from raw test results
- **Ministry Export**: Export grades in Ministry of Education-compatible Excel format
- **Multi-Teacher Support**: Secure authentication with data isolation between teachers

### Key Benefits
- Eliminates double data entry (notebook → system)
- Mobile-first design for on-field grade entry
- Automatic grade calculation with configurable formulas
- Direct export to Ministry format
- Support for Hebrew language and Israeli grade levels (י, יא, יב)

## Technology Stack

### Backend
- **Java 23** with Spring Boot 3.2
- **Spring Data JPA** for database access
- **PostgreSQL** for data storage
- **Apache POI** for Excel import/export
- **Spring Security** with JWT authentication
- **jqwik** for property-based testing

### Frontend
- **React 18** with TypeScript
- **Vite** for build tooling
- **React Router** for navigation
- **Axios** for API communication
- **TanStack Query** for state management
- **Vitest** for testing

## Getting Started

### Prerequisites
- Java 23 or higher
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL 14+

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE pe_grades;
CREATE USER pe_admin WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE pe_grades TO pe_admin;
```

2. Update `src/main/resources/application.properties` with your database credentials.

### Backend Setup

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

The backend will be available at http://localhost:8080

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

The frontend will be available at http://localhost:3000

## Project Structure

```
.
├── src/main/java/com/pe/grademanagement/
│   ├── entity/              # JPA entities
│   ├── repository/          # Spring Data repositories
│   ├── service/             # Business logic
│   ├── controller/          # REST API controllers
│   ├── security/            # Authentication & authorization
│   ├── util/                # Utility classes
│   └── dto/                 # Data transfer objects
├── src/main/resources/
│   ├── application.properties
│   └── application-test.properties
├── src/test/java/           # Unit and integration tests
├── frontend/
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── contexts/        # React contexts
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   └── types/           # TypeScript types
│   └── package.json
└── pom.xml
```

## API Documentation

### Authentication
- `POST /api/auth/login` - Teacher login
- `POST /api/auth/logout` - Teacher logout

### Student Management
- `POST /api/students/import` - Import students from Excel
- `GET /api/students/by-grade-and-class` - Get students grouped
- `GET /api/students/class/{classId}` - Get students in class

### Test Management
- `POST /api/tests` - Create test configuration
- `PUT /api/tests/{id}` - Update test configuration
- `POST /api/tests/{id}/assign` - Assign test to classes
- `GET /api/tests/class/{classId}` - Get tests for class

### Grade Entry
- `GET /api/grades/class/{classId}/test/{testId}` - Get results
- `POST /api/grades` - Save single test result
- `POST /api/grades/bulk` - Bulk save test results

### Export
- `POST /api/export/excel` - Export grades to Excel

## Testing

### Backend Tests
```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Test

# Run only property-based tests
mvn test -Dtest=*Properties
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Configuration

### Application Properties

Key configuration options in `application.properties`:

- `spring.datasource.url` - Database connection URL
- `jwt.secret` - JWT signing secret (change in production!)
- `jwt.expiration` - JWT token expiration time (milliseconds)
- `cors.allowed-origins` - Allowed CORS origins
- `spring.servlet.multipart.max-file-size` - Max upload file size

### Environment-Specific Configuration

Create `application-local.properties` or `application-prod.properties` for environment-specific settings.

## Security Considerations

- Change default JWT secret in production
- Use strong database passwords
- Enable HTTPS in production
- Configure CORS appropriately for production domains
- Regularly update dependencies for security patches

## Mobile Support

The application is optimized for mobile devices:
- Minimum supported width: 375px
- Touch-optimized inputs
- Font sizes ≥16px to prevent iOS zoom
- Responsive layouts for all screen sizes

## Hebrew Language Support

The system fully supports Hebrew:
- RTL (right-to-left) layout
- Hebrew character support in all text fields
- Israeli grade levels (י, יא, יב)
- Hebrew names and class names

## License

Proprietary - All rights reserved

## Support

For issues or questions, please contact the development team.
