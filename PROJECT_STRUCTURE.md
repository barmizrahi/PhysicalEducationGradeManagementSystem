# Project Structure

This document provides an overview of the Physical Education Grade Management System project structure.

## Root Directory

```
pe-grade-management-system/
├── src/                          # Backend source code (Java/Spring Boot)
├── frontend/                     # Frontend source code (React/TypeScript)
├── database/                     # Database scripts and migrations
├── .kiro/                        # Kiro AI specifications and tasks
├── pom.xml                       # Maven project configuration
├── docker-compose.yml            # Docker services configuration
├── start-dev.sh                  # Development startup script (Linux/macOS)
├── start-dev.bat                 # Development startup script (Windows)
├── README.md                     # Project overview and documentation
├── SETUP.md                      # Detailed setup instructions
├── PROJECT_STRUCTURE.md          # This file
└── .gitignore                    # Git ignore rules
```

## Backend Structure (src/)

```
src/
├── main/
│   ├── java/com/pe/grademanagement/
│   │   ├── GradeManagementApplication.java    # Main Spring Boot application
│   │   ├── config/                             # Configuration classes
│   │   │   └── CorsConfig.java                 # CORS configuration
│   │   ├── entity/                             # JPA entities (domain models)
│   │   │   ├── Teacher.java                    # Teacher entity (existing)
│   │   │   ├── Class.java                      # Class entity (existing)
│   │   │   ├── Student.java                    # Student entity (existing)
│   │   │   ├── Test.java                       # Test entity (to be created)
│   │   │   ├── TestAssignment.java             # Test assignment entity (to be created)
│   │   │   └── TestResult.java                 # Test result entity (to be created)
│   │   ├── repository/                         # Spring Data JPA repositories
│   │   │   └── (to be created in Task 3)
│   │   ├── service/                            # Business logic layer
│   │   │   └── (to be created in Tasks 4-10)
│   │   ├── controller/                         # REST API controllers
│   │   │   └── (to be created in Task 16)
│   │   ├── security/                           # Security configuration
│   │   │   └── (to be created in Task 14)
│   │   ├── dto/                                # Data Transfer Objects
│   │   │   └── (to be created as needed)
│   │   └── util/                               # Utility classes
│   │       └── (to be created in Tasks 4, 7, 13)
│   └── resources/
│       ├── application.properties              # Main configuration
│       └── application-test.properties         # Test configuration
└── test/
    └── java/com/pe/grademanagement/           # Test classes
        └── (to be created throughout tasks)
```

## Frontend Structure (frontend/)

```
frontend/
├── src/
│   ├── main.tsx                    # Application entry point
│   ├── App.tsx                     # Root component with routing
│   ├── index.css                   # Global styles
│   ├── contexts/                   # React contexts
│   │   └── AuthContext.tsx         # Authentication context
│   ├── components/                 # Reusable UI components
│   │   └── (to be created in Tasks 17-22)
│   ├── pages/                      # Page components
│   │   └── (to be created in Tasks 18-22)
│   ├── services/                   # API service layer
│   │   └── (to be created as needed)
│   ├── types/                      # TypeScript type definitions
│   │   └── (to be created as needed)
│   ├── utils/                      # Utility functions
│   │   └── (to be created as needed)
│   └── test/                       # Test utilities
│       └── setup.ts                # Test setup configuration
├── public/                         # Static assets
├── index.html                      # HTML template
├── package.json                    # npm dependencies and scripts
├── vite.config.ts                  # Vite configuration
├── tsconfig.json                   # TypeScript configuration
├── .eslintrc.cjs                   # ESLint configuration
└── README.md                       # Frontend documentation
```

## Database Structure (database/)

```
database/
└── init.sql                        # Database initialization script
```

## Configuration Files

### Backend Configuration
- **pom.xml**: Maven dependencies and build configuration
  - Spring Boot 3.2.1
  - Java 23
  - PostgreSQL driver
  - Apache POI for Excel
  - JWT libraries
  - jqwik for property-based testing

- **application.properties**: Runtime configuration
  - Database connection
  - JWT settings
  - CORS configuration
  - File upload limits
  - Logging levels

- **application-test.properties**: Test environment configuration
  - H2 in-memory database
  - Test-specific settings

### Frontend Configuration
- **package.json**: npm dependencies and scripts
  - React 18
  - TypeScript
  - Vite
  - React Router
  - Axios
  - TanStack Query
  - Testing libraries

- **vite.config.ts**: Build tool configuration
  - Development server settings
  - API proxy configuration
  - Test configuration

- **tsconfig.json**: TypeScript compiler options
  - Strict type checking
  - Path aliases
  - JSX configuration

### Docker Configuration
- **docker-compose.yml**: Container orchestration
  - PostgreSQL database service
  - pgAdmin (optional, for database management)
  - Network configuration
  - Volume management

## Key Directories to Create

The following directories will be created as you progress through the implementation tasks:

### Backend (Task 2-16)
- `src/main/java/com/pe/grademanagement/repository/`
- `src/main/java/com/pe/grademanagement/service/`
- `src/main/java/com/pe/grademanagement/controller/`
- `src/main/java/com/pe/grademanagement/security/`
- `src/main/java/com/pe/grademanagement/dto/`
- `src/main/java/com/pe/grademanagement/util/`
- `src/test/java/com/pe/grademanagement/`

### Frontend (Task 17-22)
- `frontend/src/components/`
- `frontend/src/pages/`
- `frontend/src/services/`
- `frontend/src/types/`
- `frontend/src/utils/`

## File Naming Conventions

### Backend (Java)
- **Entities**: `EntityName.java` (e.g., `Student.java`, `TestResult.java`)
- **Repositories**: `EntityNameRepository.java` (e.g., `StudentRepository.java`)
- **Services**: `EntityNameService.java` (e.g., `StudentService.java`)
- **Controllers**: `EntityNameController.java` (e.g., `StudentController.java`)
- **DTOs**: `EntityNameDTO.java` or `EntityNameRequest.java`/`EntityNameResponse.java`
- **Tests**: `ClassNameTest.java` for unit tests, `ClassNameProperties.java` for property tests

### Frontend (TypeScript/React)
- **Components**: `ComponentName.tsx` (PascalCase)
- **Pages**: `PageName.tsx` (PascalCase)
- **Services**: `serviceName.ts` (camelCase)
- **Types**: `types.ts` or `entityName.types.ts`
- **Utils**: `utilityName.ts` (camelCase)
- **Tests**: `ComponentName.test.tsx`

## Package Organization

### Backend Packages
```
com.pe.grademanagement
├── config          # Configuration classes (@Configuration)
├── entity          # JPA entities (@Entity)
├── repository      # Spring Data repositories (@Repository)
├── service         # Business logic (@Service)
├── controller      # REST controllers (@RestController)
├── security        # Security configuration
├── dto             # Data transfer objects
├── util            # Utility classes
└── exception       # Custom exceptions (to be created)
```

### Frontend Modules
```
src/
├── components      # Reusable UI components
├── pages           # Page-level components
├── contexts        # React contexts (state management)
├── services        # API communication layer
├── types           # TypeScript type definitions
├── utils           # Helper functions
└── hooks           # Custom React hooks (to be created)
```

## Technology Stack Summary

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 23
- **Database**: PostgreSQL 14+
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security + JWT
- **Excel**: Apache POI
- **Testing**: JUnit 5, jqwik (property-based testing)
- **Build**: Maven

### Frontend
- **Framework**: React 18
- **Language**: TypeScript
- **Build Tool**: Vite
- **Routing**: React Router 6
- **HTTP Client**: Axios
- **State Management**: TanStack Query (React Query)
- **Testing**: Vitest, React Testing Library
- **Package Manager**: npm

### Infrastructure
- **Database**: PostgreSQL (Docker)
- **Containerization**: Docker Compose
- **Development**: Hot reload (Spring DevTools, Vite HMR)

## Next Steps

1. Complete Task 2: Create core domain entities
2. Complete Task 3: Create Spring Data JPA repositories
3. Continue with subsequent tasks as outlined in `tasks.md`

## Notes

- The project uses a standard three-tier architecture (Presentation → Business Logic → Data Access)
- Backend and frontend are separate projects that communicate via REST API
- CORS is configured for local development (frontend on port 3000, backend on port 8080)
- Database schema is managed by JPA/Hibernate (auto-update in development)
- All configuration is externalized in properties files
- Hebrew language support is built-in (RTL layout, Hebrew characters)
