@echo off
REM Development startup script for PE Grade Management System (Windows)

echo Starting PE Grade Management System Development Environment
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo Docker is not running. Please start Docker and try again.
    exit /b 1
)

REM Start PostgreSQL database
echo Starting PostgreSQL database...
docker-compose up -d postgres

REM Wait for database to be ready
echo Waiting for database to be ready...
timeout /t 10 /nobreak >nul

REM Start backend
echo.
echo Starting Spring Boot backend...
echo Backend will be available at: http://localhost:8080
start "PE Grades Backend" cmd /k mvn spring-boot:run

REM Wait for backend to start
timeout /t 5 /nobreak >nul

REM Start frontend
echo.
echo Starting React frontend...
echo Frontend will be available at: http://localhost:3000
cd frontend
call npm install
call npm run dev

echo.
echo Press Ctrl+C to stop all services
pause
