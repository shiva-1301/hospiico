@echo off
REM Quick Start Script for Hospico MongoDB Setup

echo.
echo ═══════════════════════════════════════════════════════════
echo         Hospico - Hospital Finder - Quick Start
echo ═══════════════════════════════════════════════════════════
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker is not installed or not in PATH
    echo.
    echo Please install Docker from: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo ✓ Docker found
echo.
echo Starting MongoDB, Backend, and Frontend...
echo.

REM Start all services
docker-compose up --build

pause
