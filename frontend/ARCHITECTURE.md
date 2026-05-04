# Frontend Architecture

## Overview

The PE Grade Management System frontend is built with React 18, TypeScript, and Vite. It follows a component-based architecture with clear separation of concerns.

## Directory Structure

```
frontend/src/
├── api/                    # API client and service modules
│   ├── client.ts          # Axios instance with interceptors
│   ├── auth.ts            # Authentication API
│   ├── students.ts        # Student management API
│   ├── tests.ts           # Test management API
│   ├── grades.ts          # Grade entry API
│   ├── export.ts          # Export API
│   └── index.ts           # Centralized exports
├── components/            # Reusable UI components
│   └── ProtectedRoute.tsx # Authentication guard
├── contexts/              # React contexts
│   └── AuthContext.tsx    # Authentication state
├── types/                 # TypeScript type definitions
│   └── index.ts           # Shared types
├── utils/                 # Utility functions
│   ├── helpers.ts         # Common helpers
│   └── helpers.test.ts    # Helper tests
├── test/                  # Test configuration
│   └── setup.ts           # Vitest setup
├── App.tsx                # Main app with routing
├── main.tsx               # Application entry point
└── index.css              # Global styles
```

## Architecture Layers

### 1. API Layer (`src/api/`)

**Purpose**: Centralize all backend communication

**Components**:
- `client.ts`: Configured Axios instance with:
  - Base URL configuration
  - Request interceptor for JWT token injection
  - Response interceptor for error handling (401 redirects)
  - 10-second timeout
  
- Service modules (`auth.ts`, `students.ts`, etc.):
  - Type-safe API methods
  - Consistent error handling
  - Request/response transformation

**Usage Example**:
```typescript
import { studentsApi } from '@/api'

const students = await studentsApi.getStudentsByClass(classId)
```

### 2. Context Layer (`src/contexts/`)

**Purpose**: Manage global application state

**AuthContext**:
- User authentication state
- JWT token management
- Login/logout functionality
- Persistent authentication (localStorage)
- Automatic token injection into API requests

**Usage Example**:
```typescript
import { useAuth } from '@/contexts/AuthContext'

const { user, isAuthenticated, login, logout } = useAuth()
```

### 3. Component Layer (`src/components/`)

**Purpose**: Reusable UI components

**Current Components**:
- `ProtectedRoute`: Authentication guard for routes

**Future Components** (to be implemented):
- Form components (Input, Select, Button)
- Layout components (Header, Sidebar, Container)
- Feature-specific components (StudentList, GradeEntryForm, etc.)

### 4. Type Layer (`src/types/`)

**Purpose**: TypeScript type definitions

**Includes**:
- API request/response types
- Entity types (Student, Test, TestResult, etc.)
- Enum types (CalculationType, UnitType)
- Error types

**Benefits**:
- Type safety across the application
- IntelliSense support
- Compile-time error detection
- Self-documenting code

### 5. Utility Layer (`src/utils/`)

**Purpose**: Shared helper functions

**Current Utilities**:
- Grade formatting
- Time conversion (mm:ss ↔ decimal)
- Date formatting
- Validation helpers
- Error message extraction
- Debounce function

## Data Flow

### Authentication Flow

```
1. User enters credentials
   ↓
2. Login component calls authApi.login()
   ↓
3. API client sends POST /api/auth/login
   ↓
4. Backend validates and returns JWT token
   ↓
5. AuthContext stores token in localStorage
   ↓
6. Axios interceptor adds token to all requests
   ↓
7. User is redirected to dashboard
```

### Protected Route Flow

```
1. User navigates to protected route
   ↓
2. ProtectedRoute checks isAuthenticated
   ↓
3a. If authenticated → Render component
3b. If not authenticated → Redirect to /login
```

### API Request Flow

```
1. Component calls API service method
   ↓
2. Request interceptor adds JWT token
   ↓
3. Request sent to backend
   ↓
4. Response received
   ↓
5a. Success → Return data to component
5b. 401 Error → Redirect to login
5c. Other Error → Return error to component
```

## State Management Strategy

### Local State (useState)
- Component-specific UI state
- Form inputs
- Modal visibility
- Loading states

### Context State (React Context)
- Authentication state
- User information
- Global UI preferences

### Server State (React Query)
- API data caching
- Automatic refetching
- Optimistic updates
- Background synchronization

**React Query Configuration**:
```typescript
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false, // Don't refetch on window focus
      retry: 1, // Retry failed requests once
    },
  },
})
```

## Routing Strategy

### Route Structure

```
/                          → Redirect to /dashboard
/login                     → Public: Login page
/dashboard                 → Protected: Main dashboard
/students                  → Protected: Student list
/students/import           → Protected: Import students
/tests                     → Protected: Test management
/grades                    → Protected: Grade entry
/export                    → Protected: Export grades
```

### Route Protection

All routes except `/login` are protected with `ProtectedRoute` component:
- Checks authentication status
- Redirects to login if not authenticated
- Preserves intended destination for post-login redirect

## Error Handling Strategy

### API Errors

**Centralized in Axios Interceptor**:
```typescript
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear auth and redirect to login
    }
    return Promise.reject(error)
  }
)
```

**Component-Level Handling**:
```typescript
try {
  await studentsApi.importStudents(file, mapping)
} catch (error) {
  const message = getErrorMessage(error)
  // Display error to user
}
```

### Validation Errors

**Client-Side**:
- Input validation before API calls
- Inline error messages
- Prevent invalid submissions

**Server-Side**:
- Display backend validation errors
- Map field-specific errors to form fields

## Mobile-First Design

### Responsive Breakpoints

```css
/* Mobile: < 768px (default) */
/* Tablet: 768px - 1024px */
/* Desktop: > 1024px */
```

### Mobile Optimizations

1. **Input Font Size**: Minimum 16px to prevent iOS zoom
2. **Touch Targets**: Minimum 44x44px for buttons
3. **Viewport**: Proper meta viewport configuration
4. **RTL Support**: Hebrew text alignment
5. **Auto-save**: Prevent data loss on mobile

## Testing Strategy

### Unit Tests (Vitest)

**Test Coverage**:
- Utility functions (helpers.ts)
- API service methods (mocked)
- Context providers
- Custom hooks

**Example**:
```typescript
describe('parseTimeToDecimal', () => {
  it('should convert mm:ss to decimal', () => {
    expect(parseTimeToDecimal('10:30')).toBe(10.5)
  })
})
```

### Component Tests (React Testing Library)

**Test Coverage**:
- Component rendering
- User interactions
- Form submissions
- Error states

**Example**:
```typescript
it('should redirect to login when not authenticated', () => {
  render(<ProtectedRoute><Dashboard /></ProtectedRoute>)
  expect(screen.queryByText('Dashboard')).not.toBeInTheDocument()
})
```

### Integration Tests

**Test Coverage**:
- API integration
- Authentication flow
- Route navigation
- Data persistence

## Performance Considerations

### Code Splitting

- Lazy load route components
- Split vendor bundles
- Dynamic imports for large dependencies

### Caching Strategy

**React Query**:
- Cache API responses
- Stale-while-revalidate pattern
- Optimistic updates for better UX

**Browser Storage**:
- localStorage for authentication
- sessionStorage for temporary data

### Bundle Optimization

- Tree shaking (Vite)
- Minification
- Compression (gzip/brotli)
- Asset optimization

## Security Considerations

### Authentication

- JWT tokens in localStorage
- Automatic token expiration handling
- Secure token transmission (HTTPS in production)

### XSS Prevention

- React's built-in XSS protection
- Sanitize user input
- Content Security Policy headers

### CSRF Protection

- Backend CSRF tokens
- SameSite cookie attributes

## Accessibility

### WCAG 2.1 Compliance

- Semantic HTML
- ARIA labels where needed
- Keyboard navigation
- Focus management
- Screen reader support

### RTL Support

- Hebrew text alignment
- Mirrored layouts
- Proper text direction

## Future Enhancements

### Planned Features

1. **Offline Support**: Service workers for offline functionality
2. **PWA**: Progressive Web App capabilities
3. **Real-time Updates**: WebSocket for live data
4. **Advanced Caching**: IndexedDB for large datasets
5. **Analytics**: User behavior tracking
6. **Error Reporting**: Sentry integration

### Scalability Considerations

- Component library (Storybook)
- Design system
- Micro-frontends (if needed)
- CDN for static assets
- Performance monitoring
