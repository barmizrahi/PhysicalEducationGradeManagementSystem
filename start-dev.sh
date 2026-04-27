#!/bin/bash

# Development startup script for PE Grade Management System

echo "🚀 Starting PE Grade Management System Development Environment"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Start PostgreSQL database
echo "📦 Starting PostgreSQL database..."
docker-compose up -d postgres

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check database health
if docker-compose ps postgres | grep -q "healthy"; then
    echo "✅ Database is ready"
else
    echo "⚠️  Database may not be fully ready yet. Waiting a bit more..."
    sleep 5
fi

# Start backend in background
echo ""
echo "🔧 Starting Spring Boot backend..."
echo "   Backend will be available at: http://localhost:8080"
echo "   Logs: ./backend.log"
mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"

# Wait a bit for backend to start
sleep 3

# Start frontend
echo ""
echo "⚛️  Starting React frontend..."
echo "   Frontend will be available at: http://localhost:3000"
cd frontend
npm install
npm run dev

# Cleanup function
cleanup() {
    echo ""
    echo "🛑 Shutting down..."
    kill $BACKEND_PID 2>/dev/null
    docker-compose down
    echo "✅ Shutdown complete"
}

# Register cleanup function
trap cleanup EXIT INT TERM
