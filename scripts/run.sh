#!/bin/bash

# Pokemon API - Scripts de utilidad
# Uso: ./scripts/run.sh [comando]

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}"
    echo "🐾 Pokemon API - Arquitectura Limpia"
    echo "======================================"
    echo -e "${NC}"
}

print_usage() {
    echo -e "${YELLOW}Uso:${NC} ./scripts/run.sh [comando]"
    echo ""
    echo -e "${YELLOW}Comandos disponibles:${NC}"
    echo "  dev        - Ejecutar en modo desarrollo"
    echo "  test       - Ejecutar todos los tests"
    echo "  build      - Compilar el proyecto"
    echo "  docker     - Ejecutar con Docker Compose"
    echo "  clean      - Limpiar proyecto"
    echo "  docs       - Abrir documentación Swagger"
    echo "  logs       - Ver logs de la aplicación"
    echo "  help       - Mostrar esta ayuda"
}

run_dev() {
    echo -e "${GREEN}🚀 Ejecutando en modo desarrollo...${NC}"
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
}

run_tests() {
    echo -e "${GREEN}🧪 Ejecutando tests...${NC}"
    mvn clean test
}

build_project() {
    echo -e "${GREEN}🔨 Compilando proyecto...${NC}"
    mvn clean compile
    mvn package -DskipTests
}

run_docker() {
    echo -e "${GREEN}🐳 Ejecutando con Docker Compose...${NC}"
    
    # Compilar primero
    build_project
    
    # Ejecutar docker-compose
    docker-compose up --build -d
    
    echo -e "${GREEN}✅ Aplicación ejecutándose en:${NC}"
    echo "  - API: http://localhost:8080/api/v1"
    echo "  - Swagger: http://localhost:8080/api/v1/swagger-ui.html"
    echo "  - Adminer: http://localhost:8081"
    
    echo -e "\n${YELLOW}Para ver logs:${NC} docker-compose logs -f pokemon-api"
    echo -e "${YELLOW}Para detener:${NC} docker-compose down"
}

clean_project() {
    echo -e "${GREEN}🧹 Limpiando proyecto...${NC}"
    mvn clean
    docker-compose down --volumes --remove-orphans 2>/dev/null || true
    docker system prune -f 2>/dev/null || true
}

open_docs() {
    echo -e "${GREEN}📖 Abriendo documentación Swagger...${NC}"
    
    # Verificar si la aplicación está ejecutándose
    if curl -s http://localhost:8080/api/v1/actuator/health > /dev/null 2>&1; then
        if command -v open > /dev/null 2>&1; then
            open "http://localhost:8080/api/v1/swagger-ui.html"
        elif command -v xdg-open > /dev/null 2>&1; then
            xdg-open "http://localhost:8080/api/v1/swagger-ui.html"
        else
            echo "🌐 Swagger UI: http://localhost:8080/api/v1/swagger-ui.html"
        fi
    else
        echo -e "${RED}❌ La aplicación no está ejecutándose en localhost:8080${NC}"
        echo "   Ejecuta primero: ./scripts/run.sh dev o ./scripts/run.sh docker"
    fi
}

show_logs() {
    echo -e "${GREEN}📋 Mostrando logs...${NC}"
    
    # Verificar si existe el archivo de logs
    if [ -f "logs/pokemon-api.log" ]; then
        tail -f logs/pokemon-api.log
    elif docker-compose ps | grep -q pokemon-api; then
        docker-compose logs -f pokemon-api
    else
        echo -e "${RED}❌ No se encontraron logs. La aplicación no está ejecutándose.${NC}"
    fi
}

# Main script
print_header

case "${1:-help}" in
    "dev")
        run_dev
        ;;
    "test")
        run_tests
        ;;
    "build")
        build_project
        ;;
    "docker")
        run_docker
        ;;
    "clean")
        clean_project
        ;;
    "docs")
        open_docs
        ;;
    "logs")
        show_logs
        ;;
    "help"|*)
        print_usage
        ;;
esac 