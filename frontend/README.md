# PE Grade Management System - Frontend

React + TypeScript frontend for the Physical Education Grade Management System.

## Features

- Mobile-first responsive design
- RTL support for Hebrew
- JWT authentication
- Real-time grade calculation
- Auto-save functionality
- Excel import/export

## Tech Stack

- React 18
- TypeScript
- Vite
- React Router
- Axios
- TanStack Query (React Query)
- Vitest + React Testing Library

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

The application will be available at http://localhost:3000

### Build

```bash
npm run build
```

### Testing

```bash
npm test
```

## Project Structure

```
src/
├── components/       # Reusable UI components
├── contexts/         # React contexts (Auth, etc.)
├── pages/           # Page components
├── services/        # API services
├── types/           # TypeScript type definitions
├── utils/           # Utility functions
└── test/            # Test utilities and setup
```

## Configuration

The frontend proxies API requests to the backend server running on port 8080.
This is configured in `vite.config.ts`.

## Mobile Support

- Minimum supported width: 375px
- Touch-optimized inputs
- Font sizes ≥16px to prevent iOS zoom
- Responsive layouts for all screen sizes
