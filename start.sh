#!/bin/bash

# Quick Start Script for Hospico MongoDB Setup

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "         Hospico - Hospital Finder - Quick Start"
echo "═══════════════════════════════════════════════════════════"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed"
    echo ""
    echo "Please install Docker from: https://www.docker.com/products/docker-desktop"
    exit 1
fi

echo "✓ Docker found"
echo ""
echo "Starting MongoDB, Backend, and Frontend..."
echo ""

# Start all services
docker-compose up --build

