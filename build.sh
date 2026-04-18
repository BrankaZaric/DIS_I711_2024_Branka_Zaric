#!/bin/bash

echo "Building E-Commerce Microservices..."

# Build parent project and all modules
mvn clean install -DskipTests

echo "Build completed!"
