# Frontend Setup Guide

## Prerequisites

Before running the frontend application, ensure you have the following installed:

- **Node.js** (v18 or higher) - [Download here](https://nodejs.org/)
- **npm** (comes with Node.js) or **yarn**

To verify installation:
```bash
node --version
npm --version
```

## Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

## Development

### Running the Development Server

Start the Vite development server:
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

The dev server includes:
- Hot Module Replacement (HMR) for instant updates
- Proxy configuration to forward `/api` requests to `http://localhost:8080`

### Building for Production

Create an optimized production build:
```bash
npm run build
```

The build output will be in the `dist` directory.

### Preview Production Build

Preview the production build locally:
```bash
npm run preview
```

## Testing

### Run Tests

Run all tests with Vitest:
```bash
npm test
```

### Run Tests with UI

Run tests with the Vitest UI:
```bash
npm run test:ui
```

## Linting

Run ESLint to check code quality:
```bash
npm run lint
```

## Project Structure

```
frontend/
├── src/
│   ├── api/              # API client and service modules
│   │   ├── client.ts     # Axios instance with interceptors
│   │   ├── auth.ts       # Authentication API
│   │   ├── students.ts   # Student management API
│   │   ├── tests.ts      # Test management API
│   │   ├── grades.ts     # Grade entry API
│   │   ├── export.ts     # Export API
│   │   └── index.ts      # API exports
│   ├── contexts/         # React contexts
│   │   └── AuthContext.tsx  # Authentication context
│   ├── types/            # TypeScript type definitions
│   │   └── index.ts      # Shared types
│   ├── test/             # Test setup
│   │   └── setup.ts      # Vitest configuration
│   ├── App.tsx           # Main app component with routing
│   ├── main.tsx          # Application entry point
│   └── index.css         # Global styles
├── index.html            # HTML template
├── vite.config.ts        # Vite configuration
├── tsconfig.json         # TypeScript configuration
├── package.json          # Dependencies and scripts
└── SETUP.md             # This file
```

## Technology Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **React Router 6** - Client-side routing
- **Axios** - HTTP client
- **React Query** - Server state management
- **Vitest** - Testing framework
- **React Testing Library** - Component testing

## Configuration

### API Proxy

The Vite dev server is configured to proxy API requests to the backend:

```typescript
// vite.config.ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

This means requests to `/api/*` will be forwarded to `http://localhost:8080/api/*`

### TypeScript Path Aliases

The project is configured with path aliases for cleaner imports:

```typescript
// tsconfig.json
"paths": {
  "@/*": ["./src/*"]
}
```

Usage example:
```typescript
import { authApi } from '@/api'
import { User } from '@/types'
```

### Authentication

The application uses JWT-based authentication:

1. Login credentials are sent to `/api/auth/login`
2. JWT token is stored in `localStorage`
3. Token is automatically included in all API requests via Axios interceptor
4. On 401 responses, user is redirected to login page

### Mobile-First Design

The application is optimized for mobile devices:

- Minimum 16px font size for inputs (prevents iOS zoom)
- Touch-friendly UI elements
- Responsive layout with mobile breakpoints
- RTL support for Hebrew text

## Environment Variables

Create a `.env` file in the frontend directory for environment-specific configuration:

```env
# API base URL (optional, defaults to /api)
VITE_API_BASE_URL=/api

# Other environment variables as needed
```

Access in code:
```typescript
const apiUrl = import.meta.env.VITE_API_BASE_URL
```

## Troubleshooting

### Port Already in Use

If port 3000 is already in use, you can change it in `vite.config.ts`:

```typescript
server: {
  port: 3001, // Change to any available port
}
```

### Backend Connection Issues

Ensure the backend server is running on `http://localhost:8080` before starting the frontend.

### Module Not Found Errors

If you encounter module not found errors:
1. Delete `node_modules` and `package-lock.json`
2. Run `npm install` again

### TypeScript Errors

If TypeScript errors persist:
1. Restart your IDE/editor
2. Run `npm run build` to check for compilation errors

## Next Steps

After setup, you can:

1. Start the backend server (see main project README)
2. Start the frontend dev server: `npm run dev`
3. Navigate to `http://localhost:3000`
4. Begin implementing UI components for each feature

## Additional Resources

- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
- [React Router Documentation](https://reactrouter.com/)
- [Axios Documentation](https://axios-http.com/)
- [React Query Documentation](https://tanstack.com/query/latest)
