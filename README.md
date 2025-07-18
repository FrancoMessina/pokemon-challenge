# 🐾 Pokemon API - Arquitectura Limpia

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

API REST desarrollada con **Spring Boot** que permite gestionar Pokémon obteniendo información desde la [PokeAPI](https://pokeapi.co/) y almacenándola en una base de datos relacional. Implementa **arquitectura limpia**, **cache inteligente**, **documentación Swagger** y **testing exhaustivo**.

## 🎯 Características Principales

- ✅ **Arquitectura Limpia** - Separación clara de responsabilidades (Controller → Service → Repository → Entity)
- 🚀 **Cache Inteligente** - Múltiples niveles de cache con Caffeine para optimizar rendimiento
- 📖 **Documentación Swagger** - API completamente documentada y explorable
- 🧪 **Testing Completo** - Tests unitarios e integración con JUnit 5 y Mockito
- 🔍 **Logging Estructurado** - Logs detallados para monitoreo y debugging
- 🛡️ **Manejo de Errores** - Respuestas consistentes y manejador global de excepciones
- 📊 **Paginación y Filtros** - Búsqueda avanzada por nombre, tipo y paginación
- ⚡ **Validaciones** - Validación exhaustiva de entrada con Bean Validation
- 🐳 **Docker Ready** - Configuración lista para contenedores

## 🏗️ Arquitectura

```
src/
├── main/java/com/pokemon/
│   ├── PokemonApiApplication.java          # Clase principal
│   ├── application/                        # Capa de Aplicación
│   │   ├── dto/                           # DTOs de entrada y salida
│   │   ├── mapper/                        # Mappers (MapStruct)
│   │   └── service/                       # Servicios de negocio
│   ├── domain/                            # Capa de Dominio
│   │   ├── entity/                        # Entidades JPA
│   │   └── repository/                    # Interfaces de repositorio
│   └── infrastructure/                    # Capa de Infraestructura
│       ├── config/                        # Configuraciones
│       ├── external/                      # Servicios externos (PokeAPI)
│       └── web/                          # Controladores REST
└── test/                                  # Tests unitarios e integración
```

## 📋 Prerrequisitos

- **Java 17+**
- **PostgreSQL 12+** (o H2 para testing)
- **Maven 3.8+**

## 🚀 Instalación y Ejecución

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/pokemon-api.git
cd pokemon-api
```

### 2. Configurar Base de Datos
```sql
-- Crear base de datos
CREATE DATABASE pokemon_db;
CREATE USER pokemon_user WITH ENCRYPTED PASSWORD 'pokemon_pass';
GRANT ALL PRIVILEGES ON DATABASE pokemon_db TO pokemon_user;
```

### 3. Configurar aplicación
Editar `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pokemon_db
    username: pokemon_user
    password: pokemon_pass
```

### 4. Ejecutar la aplicación
```bash
# Instalar dependencias y ejecutar
mvn clean install
mvn spring-boot:run

# O ejecutar directamente
./mvnw spring-boot:run
```

La API estará disponible en: `http://localhost:8080/api/v1`

## 📚 Endpoints Principales

### 🔗 Swagger UI
**Documentación interactiva:** `http://localhost:8080/api/v1/swagger-ui.html`

### 🐾 Gestión de Pokémon

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/pokemon` | Crear nuevo Pokémon desde PokeAPI |
| `GET` | `/pokemon` | Listar todos los Pokémon (paginado) |
| `GET` | `/pokemon/{id}` | Obtener Pokémon por ID |
| `GET` | `/pokemon/name/{name}` | Obtener Pokémon por nombre |
| `GET` | `/pokemon/search?query={text}` | Buscar Pokémon por nombre parcial |
| `GET` | `/pokemon/type/{type}` | Buscar Pokémon por tipo |
| `GET` | `/pokemon/stats` | Obtener estadísticas |
| `DELETE` | `/pokemon/{id}` | Eliminar Pokémon |

## 💡 Ejemplos de Uso

### Crear un Pokémon
```bash
curl -X POST "http://localhost:8080/api/v1/pokemon" \
  -H "Content-Type: application/json" \
  -d '{"name": "pikachu"}'
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Pokémon creado exitosamente",
  "data": {
    "id": 1,
    "externalId": 25,
    "name": "pikachu",
    "height": 4,
    "weight": 60,
    "baseExperience": 112,
    "types": ["electric"],
    "abilities": ["static", "lightning-rod"],
    "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Listar Pokémon con paginación
```bash
curl "http://localhost:8080/api/v1/pokemon?page=0&size=10&sortBy=name&sortDir=asc"
```

### Buscar por tipo
```bash
curl "http://localhost:8080/api/v1/pokemon/type/electric?page=0&size=5"
```

### Búsqueda parcial por nombre
```bash
curl "http://localhost:8080/api/v1/pokemon/search?query=pika&page=0&size=10"
```

## 🧪 Testing

### Ejecutar todos los tests
```bash
mvn test
```

### Ejecutar tests con reporte de cobertura
```bash
mvn clean test jacoco:report
```

### Tests específicos
```bash
# Tests unitarios
mvn test -Dtest=PokemonServiceTest

# Tests de integración
mvn test -Dtest=PokemonControllerIntegrationTest
```

## 📊 Cache y Rendimiento

La API implementa **cache inteligente** con diferentes estrategias:

| Cache | Duración | Propósito |
|-------|----------|-----------|
| `pokemonCache` | 30 min | Datos de PokeAPI |
| `pokemonListCache` | 10 min | Listas paginadas |
| `pokemonSearchCache` | 5 min | Resultados de búsqueda |
| `pokemonExistsCache` | 15 min | Verificación de existencia |
| `pokemonStatsCache` | 2 min | Estadísticas generales |

### Métricas de Cache
Accede a: `http://localhost:8080/api/v1/actuator/metrics/cache.gets`

## 🔍 Monitoreo y Observabilidad

### Health Check
```bash
curl http://localhost:8080/api/v1/actuator/health
```

### Métricas
```bash
curl http://localhost:8080/api/v1/actuator/metrics
```

### Logs
Los logs se almacenan en: `logs/pokemon-api.log`

## 🛠️ Configuración Avanzada

### Variables de Entorno
```bash
# Base de datos
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pokemon_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

# PokeAPI
export POKEMON_API_BASE_URL=https://pokeapi.co/api/v2
export POKEMON_API_TIMEOUT=30s

# Cache
export SPRING_CACHE_CAFFEINE_SPEC=maximumSize=1000,expireAfterWrite=300s
```

### Perfiles de Spring
```bash
# Desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Testing
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🐳 Docker

### Dockerfile
```dockerfile
FROM openjdk:17-jre-slim
VOLUME /tmp
COPY target/pokemon-api-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: pokemon_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  pokemon-api:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/pokemon_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres

volumes:
  postgres_data:
```

### Ejecutar con Docker
```bash
# Construir imagen
docker build -t pokemon-api .

# Ejecutar con docker-compose
docker-compose up -d
```

## 🔧 Solución de Problemas

### Errores Comunes

**1. Error de conexión a base de datos**
```
Caused by: java.net.ConnectException: Connection refused
```
**Solución:** Verificar que PostgreSQL esté ejecutándose y las credenciales sean correctas.

**2. Error de timeout con PokeAPI**
```
Caused by: java.util.concurrent.TimeoutException
```
**Solución:** Verificar conectividad a internet o aumentar el timeout en configuración.

**3. Error de memoria en cache**
```
java.lang.OutOfMemoryError: Java heap space
```
**Solución:** Ajustar configuración de cache o aumentar memoria JVM.

### Logs útiles
```bash
# Ver logs en tiempo real
tail -f logs/pokemon-api.log

# Filtrar errores
grep ERROR logs/pokemon-api.log

# Ver métricas de cache
curl localhost:8080/api/v1/actuator/cache
```

## 📈 Roadmap

- [ ] **Autenticación JWT** - Sistema de usuarios y roles
- [ ] **Rate Limiting** - Límites de requests por IP
- [ ] **WebSocket** - Notificaciones en tiempo real
- [ ] **GraphQL** - API alternativa con GraphQL
- [ ] **Kubernetes** - Despliegue en cluster
- [ ] **CI/CD** - Pipeline automatizado
- [ ] **Monitoring** - Prometheus + Grafana

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-caracteristica`)
3. Commit cambios (`git commit -am 'Agregar nueva característica'`)
4. Push a la rama (`git push origin feature/nueva-caracteristica`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👥 Autores

- **Pokemon API Team** - *Desarrollo inicial* - [GitHub](https://github.com/pokemon-api-team)

## 🙏 Agradecimientos

- [PokeAPI](https://pokeapi.co/) - Por proporcionar la API externa de Pokémon
- [Spring Boot](https://spring.io/projects/spring-boot) - Framework principal
- [Caffeine](https://github.com/ben-manes/caffeine) - Sistema de cache
- [MapStruct](https://mapstruct.org/) - Mapeo entre objetos

---
⭐ **¡Dale una estrella si te gusta el proyecto!** ⭐ 