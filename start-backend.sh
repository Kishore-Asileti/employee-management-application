#!/usr/bin/env bash
# ================================================================
# start-backend.sh  — Build and run Spring Boot backend
# ================================================================
set -e
cd "$(dirname "$0")/backend"

echo "========================================"
echo "  Building Employee Management Backend"
echo "========================================"
mvn clean package -DskipTests

echo ""
echo "========================================"
echo "  Starting Spring Boot on port 8081"
echo "  Camunda Tasklist: http://localhost:8081/camunda/app/tasklist/"
echo "  Credentials:      admin / admin"
echo "========================================"
java -jar target/employee-management-1.0.0-SNAPSHOT.jar
